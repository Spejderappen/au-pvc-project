package vestsoft.com.pvc_project;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import vestsoft.com.api.ServerCommunication;


public class ActivityCreateUser extends Activity {

    /**
     * Keep track of the create user task to ensure we can cancel it if requested.
     */
    private UserCreateTask mCreateUserTask = null;

    private EditText mEditTextFirstName, mEditTextLastName, mEditTextPhone, mEditTextPassword;
    private Button mBtnCreateUser;
    private SharedPreferences sharedPrefs;

    private final String PROJECT_NAME = "PVC_Project";

    private View mProgressView;
    private View mCreateUserFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        Initialize();
    }

    private void Initialize() {
        mEditTextFirstName = (EditText)findViewById(R.id.editTextCreateUserFirstName);
        mEditTextLastName = (EditText)findViewById(R.id.editTextCreateUserLastName);
        mEditTextPhone = (EditText)findViewById(R.id.editTextCreateUserPhone);
        mEditTextPassword = (EditText)findViewById(R.id.editTextCreateUserPassword);

        mBtnCreateUser = (Button)findViewById(R.id.btnCreateUser);
        mBtnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewUser();
            }
        });

        mCreateUserFormView = findViewById(R.id.create_user_form);
        mProgressView = findViewById(R.id.create_user_progress);

        sharedPrefs = getSharedPreferences(PROJECT_NAME, MODE_PRIVATE);
    }

    private void CreateNewUser() {
        if (mCreateUserTask != null) {
            return;
        }

        mEditTextFirstName.setError(null);
        mEditTextLastName.setError(null);
        mEditTextPhone.setError(null);
        mEditTextPassword.setError(null);

        // Store values at the time of the create user attempt.
        String firstName = mEditTextFirstName.getText().toString();
        String lastName = mEditTextLastName.getText().toString();
        String phoneNumer = mEditTextPhone.getText().toString();
        String password = mEditTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid firstName
        if (TextUtils.isEmpty(firstName)) {
            mEditTextFirstName.setError(getString(R.string.error_field_required));
            focusView = mEditTextFirstName;
            cancel = true;
        }

        // Check for a valid lastName
        if (TextUtils.isEmpty(lastName)) {
            mEditTextLastName.setError(getString(R.string.error_field_required));
            focusView = mEditTextLastName;
            cancel = true;
        }

        // Check for a valid phone number.
        if (TextUtils.isEmpty(phoneNumer)) {
            mEditTextPhone.setError(getString(R.string.error_field_required));
            focusView = mEditTextPhone;
            cancel = true;
        } else if (!isPhoneNumberValid(phoneNumer)) {
            mEditTextPhone.setError(getString(R.string.error_invalid_phonenumber));
            focusView = mEditTextPhone;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mEditTextPassword.setError(getString(R.string.error_field_required));
            focusView = mEditTextPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mEditTextPassword.setError(getString(R.string.error_invalid_password));
            focusView = mEditTextPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt create user and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user create attempt.

            showProgress(true);
            mCreateUserTask = new UserCreateTask(firstName, lastName, phoneNumer, password, this);
            mCreateUserTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.length() >= 8 ;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mCreateUserFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mCreateUserFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mCreateUserFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mCreateUserFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous create task used to create
     * the user.
     */
    public class UserCreateTask extends AsyncTask<Void, Void, Boolean> {

        private final String mFirstName, mLastName, mPhoneNumber,mPassword;

        Context context;

        UserCreateTask(String firstName, String lastName, String phonenumber, String password, Context context) {
            mFirstName = firstName;
            mLastName = lastName;
            mPhoneNumber = phonenumber;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // attempt authentication against a network service.

            Boolean result = false;
            try {
                result = ServerCommunication.createUser(mFirstName, mLastName, mPhoneNumber, mPassword);
            } catch (Exception e) {
                Log.e("PVC", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateUserTask = null;
            showProgress(false);

            if (success) {
                SavePhoneNumber(mPhoneNumber);
                Intent showMapActIntent = new Intent(context, ActivityMaps.class);
                context.startActivity(showMapActIntent);
            } else {
                mEditTextPhone.setError("Phone number is already in use");
                mEditTextPhone.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateUserTask = null;
            showProgress(false);
        }

        private void SavePhoneNumber(String phonenumber){
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("my_phone", phonenumber);
            editor.commit();
        }
    }
}
