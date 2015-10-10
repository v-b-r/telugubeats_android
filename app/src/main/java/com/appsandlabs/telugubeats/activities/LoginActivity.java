package com.appsandlabs.telugubeats.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.view.View;

import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.loginutils.GoogleLoginHelper;
import com.appsandlabs.telugubeats.models.User;

import static com.appsandlabs.telugubeats.TeluguBeatsApp.getServerCalls;

/**
 * Created by abhinav on 10/4/15.
 */
public class LoginActivity extends AppBaseFragmentActivity {

    private View facebookButton;
    private View googlePlusButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(TeluguBeatsApp.getUserDeviceManager().isLoggedInUser()){
           goToMainActivity();
            return;
        }


        setContentView(R.layout.welcome_login_fb_gplus);
        facebookButton = findViewById(R.id.facebook_button);
        googlePlusButton = findViewById(R.id.google_plus_button);
        googlePlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GoogleLoginHelper().doLogin(new GenericListener<User>(){
                    @Override
                    public void onData(User user) {
                        getServerCalls().registerUser(user, new GenericListener<User>() {
                            @Override
                            public void onData(User user) {
                                TeluguBeatsApp.getUserDeviceManager().setPreference(Config.PREF_ENCODED_KEY, user.auth_key);
                                TeluguBeatsApp.currentUser = user;
                                goToMainActivity();
                            }
                        });



                    }
                });
            }
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
    }

}
