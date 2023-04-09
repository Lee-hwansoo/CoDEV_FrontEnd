package com.example.codev

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.codev.addpage.*
import com.example.codev.databinding.ActivityCommunityDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList


class InfoDetailActivity:AppCompatActivity() {
    private lateinit var viewBinding: ActivityCommunityDetailBinding
    private var id: Int = -1

    override fun onResume() {
        super.onResume()
        id = intent.getIntExtra("id",-1)
        if (id != -1) {
            loadInfoDetail(this, id)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCommunityDetailBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.toolbarCommunity.toolbar2.title = ""
        viewBinding.toolbarCommunity.toolbarText.text = "정보글"
        setSupportActionBar(viewBinding.toolbarCommunity.toolbar2)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.left2)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            android.R.id.home ->{
                Toast.makeText(this, "뒤로가기", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setViewMode(boolean: Boolean){
        if (boolean){
            //뷰어와 작성자 같을 때 : 작성자
            Log.d("test","작성자 모드")
        }else{
            //뷰어와 작성자 다를 때 : 뷰어
            Log.d("test","뷰어 모드")
        }
    }

    private fun loadInfoDetail(context:Context, id: Int){
        RetrofitClient.service.getInfoDetail(AndroidKeyStoreUtil.decrypt(UserSharedPreferences.getUserAccessToken(context)),id).enqueue(object : Callback<ResGetInfoDetail>{
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ResGetInfoDetail>, response: Response<ResGetInfoDetail>) {
                if(response.isSuccessful.not()){
                    Log.d("test: 조회실패",response.toString())
                    Toast.makeText(context, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    when(response.code()){
                        200->{
                            response.body()?.let {
                                Log.d("test: 정보글 조회 성공! ", "\n${it.toString()}")
                                Log.d("test: 정보글 데이터 :", "\n${it.result.Complete}")

                                //header
                                viewBinding.title.text = it.result.Complete.co_title
                                viewBinding.writerNickname.text = it.result.Complete.co_nickname
                                Glide.with(context)
                                    .load(it.result.Complete.profileImg).circleCrop()
                                    .into(viewBinding.writerProfileImg)
                                viewBinding.writeDate.text = it.result.Complete.updatedAt.toString()

                                //footer
                                viewBinding.smileCounter.text = "${it.result.Complete.co_likeCount}명이 공감해요"
                                viewBinding.commentCounter.text = "댓글 ${it.result.Complete.co_commentCount}"

                                setViewMode(it.result.Complete.co_email == it.result.Complete.co_viewer)

                                //content
                                viewBinding.contentText.text = it.result.Complete.content
                                setPhotoAdapter(it.result.Complete.co_photos)
                                setCommentAdapter(it.result.Complete.co_comment)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResGetInfoDetail>, t: Throwable) {
                Log.d("test: 조회실패 - RPF > loadData_s(스터디 전체조회)", "[Fail]${t.toString()}")
                Toast.makeText(context, "서버와 연결을 시도했으나 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setPhotoAdapter(dataList: ArrayList<InfoDetailPhoto>){
        val adapter = AdapterCommunityInfoPhotoList(this,dataList)
        viewBinding.rvImg.adapter = adapter
    }

    private fun setCommentAdapter(dataList: ArrayList<InfoDetailComment>){
        val adapter = AdapterCommunityInfoParentCommentList(this,dataList)
        viewBinding.rvComment.adapter = adapter
    }
}