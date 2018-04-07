package com.shollmann.events.ui.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shollmann.events.R;
import com.shollmann.events.sql.DatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.fourhcode.forhutils.FUtilsValidation.isEmpty;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LoginActivity";

    @BindView(R.id.img_header_logo)
    ImageView imgHeaderLogo;
    @BindView(R.id.tv_login)
    TextView tvLogin;
    @BindView(R.id.et_email)
    EditText etEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.lnlt_inputs_container)
    LinearLayout lnltInputsContainer;
    @BindView(R.id.tv_dont_have_account)
    TextView tvDontHaveAccount;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.rllt_body)
    RelativeLayout rlltBody;
    @BindView(R.id.prgs_loading)
    ProgressBar prgsLoading;
    @BindView(R.id.rllt_loading)
    RelativeLayout rlltLoading;
    @BindView(R.id.activity_login)
    RelativeLayout activityLogin;

    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        databaseHelper = new DatabaseHelper(this);

    }

    @OnClick({R.id.tv_dont_have_account, R.id.btn_login})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_dont_have_account:
                Intent intent = new Intent(this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // override default transation of activity
                this.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_out_right);
                break;
            case R.id.btn_login:
                if (!isEmpty(etEmail, getString(R.string.enter_id)) && !isEmpty(etPassword, getString(R.string.enter_password))) {
                    setLoadingMode();
                    // create new user

                    if (databaseHelper.checkUser(etEmail.getText().toString().trim(), etPassword.getText().toString().trim())) {
                        Intent accountsIntent = new Intent(this, EventsActivity.class);
                        accountsIntent.putExtra("EMAIL", etEmail.getText().toString().trim());
                        startActivity(accountsIntent);
                    } else {
                        // Snack Bar to show success message that record is wrong
                        Snackbar.make(activityLogin, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG).show();
                    }


                    setNormalMode();
                }
                break;
        }
    }

    // set loading layout visible and hide body layout
    private void setLoadingMode() {
        rlltLoading.setVisibility(View.VISIBLE);
        rlltBody.setVisibility(View.GONE);
    }

    // set body layout visible and hide loading layout
    private void setNormalMode() {
        rlltLoading.setVisibility(View.GONE);
        rlltBody.setVisibility(View.VISIBLE);
    }
}
