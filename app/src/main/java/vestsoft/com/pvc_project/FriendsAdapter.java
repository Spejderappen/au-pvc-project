package vestsoft.com.pvc_project;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vestsoft.com.pvc_project.Model.Friend;

/**
 * Created by Filip on 01-09-2014.
 */
public class FriendsAdapter  extends ArrayAdapter<Friend>{
    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private FriendsAdapterCallback mCallbacks;

        private Activity context;
        private List<Friend> friendList;

        public FriendsAdapter(Activity activity, List<Friend> friendList) {
            super(activity, R.layout.checkbox_item, friendList);
            this.context = activity;
            this.friendList = friendList;

            try {
                mCallbacks = (FriendsAdapterCallback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement FriendsAdapterCallback.");
            }
        }

        private class ViewHolder {
            TextView text;
            CheckBox checkbox;
        }

        // This method is automatically called for every item in the friendList
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                view = inflater.inflate(R.layout.checkbox_item, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.text = (TextView) view.findViewById(R.id.label);
                viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
                viewHolder.checkbox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                                         boolean isChecked) {
                                Friend element = (Friend) viewHolder.checkbox.getTag();
                                element.setSelected(buttonView.isChecked());
                                mCallbacks.onCheckBoxCheckedListener(element);

                            }
                        });
                view.setTag(viewHolder);
                viewHolder.checkbox.setTag(friendList.get(position));
            } else {
                view = convertView;
                ((ViewHolder) view.getTag()).checkbox.setTag(friendList.get(position));
            }
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text.setText(friendList.get(position).getName());
            holder.checkbox.setChecked(friendList.get(position).isSelected());
            return view;
//            ViewHolder holder = null;
//
//            if (convertView == null) {
//                LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                convertView = vi.inflate(R.layout.checkbox_item, null);
//
//                holder = new ViewHolder();
//                holder.code = (TextView) convertView.findViewById(R.id.code);
//                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
//                convertView.setTag(holder);
//
//                holder.name.setOnClickListener( new View.OnClickListener() {
//                    public void onClick(View v) {
//                        CheckBox cb = (CheckBox) v ;
//                        Friend country = (Friend) cb.getTag();
//                        country.setSelected(cb.isChecked());
//                    }
//                });
//            }
//            else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//
//            Friend country = friendList.get(position);
//            holder.code.setText(" (" + country.getPhone() + ")");
//            holder.name.setText(country.getName());
//            holder.name.setChecked(country.isSelected());
//            holder.name.setTag(country);
//
//            return convertView;

        }

    public static interface FriendsAdapterCallback {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onCheckBoxCheckedListener(Friend selectedFriend);
    }
    }
