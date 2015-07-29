package pl.comarch.soc.socmessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import pl.comarch.soc.socmessenger.model.User;

/**
 * Created by Comarch on 2015-07-24.
 */
public class UsersListAdapter extends ArrayAdapter<User> {

    public UsersListAdapter(Context context, List<User> values) {
        super(context, R.layout.user_status, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_status, parent, false);
        }

        TextView userNameTextView = (TextView) convertView.findViewById(R.id.userNameTextView);
        userNameTextView.setText(user.getName());

        TextView lastSeenTextView = (TextView) convertView.findViewById(R.id.lastSeenTextView);
        String lastSeenText = (user.getLastSeen() != null ? user.getLastSeen().toString() : "");
        lastSeenTextView.setText(lastSeenText);

        if(user.isOnline()) {
            lastSeenTextView.setVisibility(View.GONE);
            userNameTextView.setBackgroundResource(android.R.color.holo_green_light);

        } else {
            lastSeenTextView.setVisibility(View.VISIBLE);
            userNameTextView.setBackgroundResource(android.R.color.background_light);
        }

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        setNotifyOnChange(false);
        sort(new Comparator<User>() {
            @Override
            public int compare(User lhs, User rhs) {
                if(lhs.isOnline() == rhs.isOnline()) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return (lhs.isOnline() ? -1 : 1);
                }
            }
        });
        super.notifyDataSetChanged();
    }
}
