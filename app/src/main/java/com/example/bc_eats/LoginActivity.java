package com.example.bc_eats;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    private Button mLoginButton;
    private EditText mPhoneNumber_EditText;
    private TextView mHyperLink;

    public static Context mContext;
    public static String mPhone;
    public static Spanned mHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); //renders your activity_login.xml file in your res layout folder
        mContext = this;

        //initializing widgets
        mPhoneNumber_EditText = (EditText) findViewById(R.id.phone_et); //links java widget to the EditText widget in activity_login.xml file with id=phone_et
        mLoginButton = (Button)findViewById(R.id.login_bt);
        mHyperLink = (TextView)findViewById(R.id.app_disclaimer);


        //code hyperlink - java implementation
        mHyperLink.setMovementMethod(LinkMovementMethod.getInstance());
        mHtml = Html.fromHtml("If you are interested in getting involved or have suggestions, please visit our\n" +
                "        <a ref='https://github.com/sustainability-BC-Eats/BC-Eats'>github repository</a>\n" +
                "        or contact us through our <a ref='sustainability-BC-Eats@gmail.com'>email</a>");
        mHyperLink.setText(mHtml);



        //code login button
        mLoginButton.setOnClickListener(v -> {
            mPhone = mPhoneNumber_EditText.getText().toString().trim();
            if(mPhone.isEmpty())
            {
                mPhoneNumber_EditText.setError("please enter phone number");
                mPhoneNumber_EditText.requestFocus();
            }
            else
            {
                Intent intent = new Intent(mContext,PhoneAuthenticationActivity.class);
                intent.putExtra("mPhone",mPhone);
                startActivity(intent);
            }
        });
    }
}

