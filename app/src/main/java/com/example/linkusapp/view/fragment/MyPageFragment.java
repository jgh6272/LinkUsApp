package com.example.linkusapp.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.linkusapp.R;
import com.example.linkusapp.view.activity.HomeActivity;
import com.example.linkusapp.view.activity.SetAddressActivity;
import com.example.linkusapp.viewModel.LoginViewModel;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.kakao.sdk.user.UserApiClient;

public class MyPageFragment extends Fragment {

    private TextView nickNameTV,addressTV,methodTV;
    private LoginViewModel viewModel;
    private Button logout,withdraw,shareApp,setAddress;
    private String loginMethod;
    private GoogleSignInClient mSignInClient;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_page, container, false);
        nickNameTV = (TextView) view.findViewById(R.id.nickname_tv);
        addressTV = (TextView) view.findViewById(R.id.address_tv);
        methodTV = (TextView) view.findViewById(R.id.method_tv);
        logout = (Button) view.findViewById(R.id.mypage_logout);
        withdraw = (Button) view.findViewById(R.id.withdraw_app);
        shareApp = (Button) view.findViewById(R.id.share_app);
        setAddress = (Button) view.findViewById(R.id.my_address);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginMethod = viewModel.getLoginMethod();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        mSignInClient =  GoogleSignIn.getClient(getActivity(), gso);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        methodTV.setText(viewModel.getLoginMethod());
        addressTV.setText(viewModel.getAddress());
        nickNameTV.setText(viewModel.getNickname());
        userId = viewModel.getLoginSession();

        /*내지역설정*/
        setAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SetAddressActivity.class));
            }
        });

        /*앱 공유하기 버튼*/
        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent msg = new Intent(Intent.ACTION_SEND);
                msg.addCategory(Intent.CATEGORY_DEFAULT);
                msg.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.example.linkusapp");
                msg.putExtra(Intent.EXTRA_TITLE, "앱 공유하기");
                msg.setType("text/plain");
                startActivity(Intent.createChooser(msg, "앱을 선택해 주세요"));
            }
        });
        /*탈퇴하기 버튼*/
        withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.withDraw(userId,loginMethod);
            }
        });
        viewModel.withDrawREDLD.observe(getActivity(),code -> {
            if (code.equals("200")) {
                Snackbar.make(view.findViewById(R.id.my), "탈퇴 성공", Snackbar.LENGTH_SHORT).show();
                logout();
            }else {
                Snackbar.make(view.findViewById(R.id.my), "탈퇴 실패", Snackbar.LENGTH_SHORT).show();
            }
        });
        /*로그아웃 버튼*/
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }

    private void logout(){
        switch (loginMethod){
            case  "일반" :{
                //일반 로그아웃
                viewModel.cancelAutoLogin();
                break;
            }
            case "Google" :{
                //구글 로그아웃
                googleSignOut();
                break;
            }
            case "Facebook" :{
                //facebook 로그아웃
                LoginManager.getInstance().logOut();
                break;
            }
            case "Kakao" :{
                UserApiClient.getInstance().logout(error ->{
                    if(error !=null){
                        Log.e("kakao", "로그아웃 실패. SDK에서 토큰 삭제됨", error);
                    }else{
                        Log.i("kakao", "로그아웃 성공. SDK에서 토큰 삭제됨");
                    }
                    return null;
                });
                break;
            }
        }
        viewModel.removeUserIdPref();
//                viewModel.removeLoginMethod();
        startActivity(new Intent(getActivity(), HomeActivity.class));
        getActivity().overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    //로그아웃하기
    private void googleSignOut() {
        mSignInClient.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }
}