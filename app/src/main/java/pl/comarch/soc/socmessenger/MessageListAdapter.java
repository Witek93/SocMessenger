package pl.comarch.soc.socmessenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import pl.comarch.soc.socmessenger.model.Message;


public class MessageListAdapter extends ArrayAdapter<Message> {

    public MessageListAdapter(Context context, List<Message> values) {
        super(context, R.layout.message, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.message, parent, false);
        }

        TextView contentTextView = (TextView) convertView.findViewById(R.id.content);
        contentTextView.setText(message.getContent());

        TextView authorTextView = (TextView) convertView.findViewById(R.id.author);
        authorTextView.setText(message.getAuthor());

        TextView whenTextView = (TextView) convertView.findViewById(R.id.when);
        whenTextView.setText(message.getWhen().toString());


        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        setNotifyOnChange(false);
        sort(new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.getWhen().compareTo(rhs.getWhen());
            }
        });
        super.notifyDataSetChanged();
    }
}
