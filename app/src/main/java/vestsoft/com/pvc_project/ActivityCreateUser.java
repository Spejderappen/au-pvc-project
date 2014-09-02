package vestsoft.com.pvc_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

    private EditText mEditTextName, mEditTextPhone, mEditTextPassword;
    private Button mBtnCreateUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        Initialize();
    }

    private void Initialize() {
        mEditTextName = (EditText)findViewById(R.id.editTextCreateUserName);
        mEditTextPhone = (EditText)findViewById(R.id.editTextCreateUserPhone);
        mEditTextPassword = (EditText)findViewById(R.id.editTextCreateUserPassword);

        mBtnCreateUser = (Button)findViewById(R.id.btnCreateUser);
        mBtnCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewUser();
            }
        });
    }

    private void CreateNewUser() {
        if (mCreateUserTask != null) {
            return;
        }

        mEditTextName.setError(null);
        mEditTextPhone.setError(null);
        mEditTextPassword.setError(null);

        // Store values at the time of the create user attempt.
        String name = mEditTextName.getText().toString();
        String phoneNumer = mEditTextPhone.getText().toString();
        String password = mEditTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name (must have at least 3 characters
        if (!TextUtils.isEmpty(name)) {
            mEditTextName.setError(getString(R.string.error_field_required));
            focusView = mEditTextName;
            cancel = true;
        } else if (!isNameValid(name)){
            mEditTextName.setError(getString(R.string.error_invalid_name));
            focusView = mEditTextName;
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user create attempt.

            //showProgress(true);
//            mCreateUserTask = new UserLoginTask(phoneNumer, password, this);
//            mCreateUserTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 5;
    }

    private boolean isPhoneNumberValid(String phoneNumber) {
        return phoneNumber.length() >= 8 ;
    }

    private boolean isNameValid(String name) {
        return name.length() >= 3;
    }

    /**
     * Represents an asynchronous create task used to create
     * the user.
     */
    public class UserCreateTask extends AsyncTask<Void, Void, Boolean> {

        //private final String mPhoneNumber;
        private final String mPhoneNumber;
        private final String mPassword;
        Context context;

        UserCreateTask(String name, String phonenumber, String password, Context context) {
            mPhoneNumber = phonenumber;
            mPassword = password;
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // attempt authentication against a network service.

            Boolean result = false;
            try {
                result = ServerCommunication.login(mPhoneNumber, mPassword);
            } catch (Exception e) {
                Log.e("PVC", e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCreateUserTask = null;
            //showProgress(false);

            if (success) {
                Intent showMapActIntent = new Intent(context, ActivityMaps.class);
                context.startActivity(showMapActIntent);
            } else {
                //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mCreateUserTask = null;
            //showProgress(false);
        }
    }
}
