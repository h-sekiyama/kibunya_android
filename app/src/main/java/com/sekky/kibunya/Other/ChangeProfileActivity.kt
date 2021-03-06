package com.sekky.kibunya.Other

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sekky.kibunya.Common.Functions
import com.sekky.kibunya.KibunInput.KibunInputActivity
import com.sekky.kibunya.Kibunlist.MainActivity
import com.sekky.kibunya.R
import com.sekky.kibunya.Users
import com.sekky.kibunya.databinding.ActivityChangeProfileBinding
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_change_profile.*
import kotlinx.android.synthetic.main.tab_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ChangeProfileActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var beforeName: String = ""    // 変更前の名前
    private var isChangedProfIcon: Boolean = false  // 画像を更新しているか判定するフラグ

    private val binding: ActivityChangeProfileBinding by lazy {
        DataBindingUtil.setContentView<ActivityChangeProfileBinding>(this, R.layout.activity_change_profile)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${currentUser!!.uid}.jpg")

        // 名前の初期値を設定
        beforeName = currentUser.displayName.toString()

        // 画像の初期値を設定
        imageRef.getBytes(1000000).addOnSuccessListener {
            // 画像が存在すればそれを表示
            Glide.with(this)
                .load(imageRef)
                .placeholder(R.drawable.noimage)
                .diskCacheStrategy(DiskCacheStrategy.NONE )
                .skipMemoryCache(true)
                .into(binding.profileImage)
        }.addOnFailureListener {
            // 取得失敗したらデフォルト画像表示
            binding.profileImage.setImageResource(R.drawable.noimage)
        }

        // 保存ボタンタップ
        binding.profileChangeButton.setOnClickListener {
            // プログレスバー表示
            binding.overlay.visibility = View.VISIBLE
            binding.progressbar.visibility = View.VISIBLE

            saveProfile(currentUser, imageRef)
        }

        init()
    }

    // 画像をギャラリーから読み込んで戻ってきた時
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        val currentUser = auth.currentUser
        // CloudStorageを使う準備
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profileIcon/${currentUser!!.uid}.jpg")

        // 保存ボタンタップ
        binding.profileChangeButton.setOnClickListener {
            saveProfile(currentUser, imageRef)
        }

        init()

        if (requestCode == 203){
            if (resultCode != Activity.RESULT_OK){
                return
            }

            val tmpImage = resultData?.data!!
            val tmpFileName = UUID.randomUUID().toString() + ".png"
            File.createTempFile(tmpFileName, null, cacheDir)
            val tmpFileUri = Uri.fromFile(File(cacheDir, tmpFileName))

            val options = com.yalantis.ucrop.UCrop.Options()
            options.setToolbarTitle("切り抜き")
            options.setCompressionFormat(Bitmap.CompressFormat.PNG)
            options.withAspectRatio(1F, 1F)
            val uCrop = UCrop.of(tmpImage, tmpFileUri)
            uCrop.withOptions(options)
            uCrop.start(this)
        } else if (requestCode == UCrop.REQUEST_CROP){
            if (resultCode == Activity.RESULT_OK) {
                val bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(UCrop.getOutput(resultData!!)!!.encodedPath), binding.profileImage.width, binding.profileImage.height, true)
                // 保存ボタンをアクティブにする
                binding.profileChangeButton.isEnabled = true
                binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
                isChangedProfIcon = true
                runOnUiThread {
                    binding.profileImage.setImageBitmap(bitmap)
                }
            } else if (resultCode == UCrop.RESULT_ERROR){
                Log.e("TAG", "uCropエラー: "+ UCrop.getError(resultData!!).toString())
            }
        }
    }

    private fun init() {
        binding.name = beforeName

        // 背景タップでキーボードを隠すための処理
        Functions.addBackgroundFocus(binding.background, this)

        // 名前入力ボックスの入力監視
        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // nop
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChangeProfileButtonEnable()
            }

            override fun afterTextChanged(s: Editable?) {
                updateChangeProfileButtonEnable()
            }
        })

        // 戻るボタン
        binding.leftButton.setOnClickListener {
            finish()
        }

        // ギャラリーから画像選択
        binding.profileImage.setOnClickListener {
            beforeName = binding.nameInput.text.toString()
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 203)
        }

        // タブボタン処理
        binding.tabLayout.tab_button2.setImageResource(R.drawable.tab2_on)
        binding.tabLayout.tab_button0.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button1.setOnClickListener {
            val intent = Intent(this, KibunInputActivity::class.java)
            startActivity(intent)
        }
        binding.tabLayout.tab_button2.setOnClickListener {
            val intent = Intent(this, OtherActivity::class.java)
            startActivity(intent)
        }
    }

    // プロフィール変更ボタンの有効/無効の切り替え
    private fun updateChangeProfileButtonEnable() {
        if (binding.nameInput.text.count() in 1..16 && beforeName != binding.nameInput.text.toString()) {
            binding.profileChangeButton.isEnabled = true
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_enabled_30dp)
        } else {
            binding.profileChangeButton.isEnabled = false
            binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)
        }
    }

    private fun saveProfile(currentUser: FirebaseUser, imageRef: StorageReference) {
        currentUser.apply {
            val profileUpdates : UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                .setDisplayName(
                    binding.nameInput.text.toString()
                ).build()
            updateProfile(profileUpdates).addOnCompleteListener { it ->
                when (it.isSuccessful) {
                    true -> apply {
                        beforeName = binding.nameInput.text.toString()
                        binding.profileChangeCompleteLabel.visibility = View.VISIBLE
                        binding.profileChangeButton.isEnabled = false
                        binding.profileChangeButton.setBackgroundResource(R.drawable.shape_rounded_corners_disabled_30dp)

                        // usersコレクションも書き換え
                        currentUser.reload()
                        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
                        db.collection("users").document(currentUser.uid).set(
                            Users(currentUser.displayName)
                        ).addOnSuccessListener {
                            if (isChangedProfIcon) {
                                val bitmap = (binding.profileImage.drawable as BitmapDrawable).bitmap
                                val baos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                                val data = baos.toByteArray()
                                val uploadTask = imageRef.putBytes(data)
                                uploadTask.addOnFailureListener { e ->
                                    Functions.showAlertOneButton(
                                        this@ChangeProfileActivity,
                                        "エラー",
                                        e.message.toString()
                                    )
                                }.addOnSuccessListener {
                                    isChangedProfIcon = false
                                    GlobalScope.launch(Dispatchers.Default) {
                                        Glide.get(applicationContext).clearDiskCache()
                                    }
                                    Glide.get(applicationContext).clearMemory()
                                }
                            }
                        }.addOnFailureListener {
                            Functions.showAlertOneButton(this@ChangeProfileActivity, "エラー", it.message.toString())
                        }.addOnCompleteListener {
                            // プログレスバー非表示
                            binding.overlay.visibility = View.GONE
                            binding.progressbar.visibility = View.GONE
                        }
                    }
                    false -> Functions.showAlertOneButton(
                        this@ChangeProfileActivity,
                        "エラー",
                        "更新に失敗しました"
                    )
                }
            }
        }
    }
}