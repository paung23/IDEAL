/**
 * Created by Phyo Thuta Aung
 */

package edu.fandm.research.ideal.Application.AppInterface;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.fandm.research.ideal.Application.Database.PredictedLeak;
import edu.fandm.research.ideal.R;

public class PredictionListViewAdapter extends BaseAdapter {
    private final Context context;
    private List<PredictedLeak> list;

    public PredictionListViewAdapter(Context context, List<PredictedLeak> list) {
        super();
        this.context = context;
        this.list = list;
    }

    public void updateData(List<PredictedLeak> list) {
        this.list = list;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_prediction, null);
            holder = new ViewHolder();

            holder.time = (TextView) convertView.findViewById(R.id.predicted_time);
            holder.appName = (TextView) convertView.findViewById(R.id.predicted_app);
            //holder.leakCategory = (TextView) convertView.findViewById(R.id.predicted_category);
            //holder.leakType = (TextView) convertView.findViewById(R.id.predicted_type);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        PredictedLeak leak = list.get(position);
        holder.time.setText(leak.getTimestamp());
        holder.appName.setText(leak.getAppName());
        //holder.leakCategory.setText(leak.getCategory());
        //holder.leakType.setText(leak.getType());
        return convertView;
    }

    public static class ViewHolder {
        public TextView time;
        public TextView appName;
        //public TextView leakCategory;
        //public TextView leakType;
    }
}
