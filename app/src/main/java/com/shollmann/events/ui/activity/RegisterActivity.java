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

import com.fourhcode.forhutils.FUtilsValidation;
import com.shollmann.events.R;
import com.shollmann.events.model.User;
import com.shollmann.events.sql.DatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegisterActivity extends AppCompatActivity {
    private final String TAG = "RegisterActivity";

    @BindView(R.id.img_header_logo)
    ImageView imgHeaderLogo;
    @BindView(R.id.tv_login)
    TextView tvLogin;
    @BindView(R.id.et_email)
    EditText mEmail;
    @BindView(R.id.et_password)
    EditText etPassword;
    @BindView(R.id.et_repeat_password)
    EditText etRepeatPassword;
    @BindView(R.id.lnlt_inputs_container)
    LinearLayout lnltInputsContainer;
    @BindView(R.id.tv_already_have_account)
    TextView tvAlreadyHaveAccount;
    @BindView(R.id.btn_signup)
    Button btnSignup;
    @BindView(R.id.rllt_body)
    RelativeLayout rlltBody;
    @BindView(R.id.prgs_loading)
    ProgressBar prgsLoading;
    @BindView(R.id.rllt_loading)
    RelativeLayout rlltLoading;
    @BindView(R.id.activity_login)
    RelativeLayout activityLogin;

    private DatabaseHelper databaseHelper;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        databaseHelper = new DatabaseHelper(this);
        user = new User();
    }

    @OnClick({R.id.tv_already_have_account, R.id.btn_signup})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_already_have_account:
                final Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                this.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_out_left);
                break;
            case R.id.btn_signup:
                if (!FUtilsValidation.isEmpty(mEmail, getString(R.string.enter_id))
                        && !FUtilsValidation.isEmpty(etPassword, getString(R.string.enter_password))
                        && !FUtilsValidation.isEmpty(etRepeatPassword, getString(R.string.enter_password_again))
                        && FUtilsValidation.isPasswordEqual(etPassword, etRepeatPassword, getString(R.string.password_isnt_equal))
                        ) {
                    setLoadingMode();
                    // create new user object and set data from editTexts

                    if (!databaseHelper.checkUser(mEmail.getText().toString().trim())) {

                        user.setName(mEmail.getText().toString().trim());
                        user.setEmail(mEmail.getText().toString().trim());
                        user.setPassword(etPassword.getText().toString().trim());

                        databaseHelper.addUser(user);

                        // Snack Bar to show success message that record saved successfully
                        Snackbar.make(activityLogin, "Registration Successful", Snackbar.LENGTH_LONG).show();

                        startActivity(new Intent(this, EventsActivity.class));
                    } else {
                        // Snack Bar to show error message that record already exists
                        Snackbar.make(activityLogin, "Email Already Exists", Snackbar.LENGTH_LONG).show();
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
