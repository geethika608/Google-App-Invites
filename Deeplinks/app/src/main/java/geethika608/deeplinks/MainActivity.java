package geethika608.deeplinks;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    public GoogleApiClient mGoogleApiClient;
    int PERMISSION_REQUEST_ACCOUNTS = 9;
    private final int REQUEST_INVITE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String deeplink = "Your Firebase Deeplink Goes Here";
        Button share = (Button) findViewById(R.id.share);
        final EditText sharetext = (EditText) findViewById(R.id.sharetext);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetUserPermission();
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(AppInvite.API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addOnConnectionFailedListener(this)
                .build();

        boolean autoLaunchDeepLink = false;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    Toast.makeText(MainActivity.this, deepLink, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //set persmission in android 6.0
    public void SetUserPermission() {
        final String[] permissions = {Manifest.permission.GET_ACCOUNTS};
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        permissions,
                        PERMISSION_REQUEST_ACCOUNTS);
            }else{
                Google_Invite();
            }
        } else {
            Google_Invite();
        }
    }

    //get permission conformation from user for both google plus and contacts
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_ACCOUNTS) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Google_Invite();
            } else {
                // Permission request was denied.
                Toast.makeText(MainActivity.this, "we can't get friends with Google ", Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("MainActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == MainActivity.this.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("MainActivity", "onActivityResult: sent invitation " + id);
                }
            }
        }
    }

    public void Google_Invite() {
        Intent intent = new AppInviteInvitation.IntentBuilder("GoogleAppInvites")
                .setMessage("Hello there!")
                .setEmailSubject("Invitation from an app")
                .setEmailHtmlContent("Install this app and be my friend\n%%APPINVITE_LINK_PLACEHOLDER%%</p>")
                .setDeepLink(Uri.parse("http://deeplinks.com"))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }
}
