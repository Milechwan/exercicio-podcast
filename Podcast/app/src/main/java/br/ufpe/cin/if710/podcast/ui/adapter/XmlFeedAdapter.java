package br.ufpe.cin.if710.podcast.ui.adapter;

import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.db.PodcastDBHelper;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.ui.EpisodeDetailActivity;

public class XmlFeedAdapter extends ArrayAdapter<ItemFeed> {

    int linkResource;
    Context c;
   // Cursor cursor;

    public XmlFeedAdapter(Context context, int resource, List<ItemFeed> objects) {
        super(context, resource, objects);
        linkResource = resource;
        c = context;
    }

   /* public void setCursor(Cursor cursr){
        cursor = cursr;
    }*/
    /**
     * public abstract View getView (int position, View convertView, ViewGroup parent)
     * <p>
     * Added in API level 1
     * Get a View that displays the data at the specified position in the data set. You can either create a View manually or inflate it from an XML layout file. When the View is inflated, the parent View (GridView, ListView...) will apply default layout parameters unless you use inflate(int, android.view.ViewGroup, boolean) to specify a root view and to prevent attachment to the root.
     * <p>
     * Parameters
     * position	The position of the item within the adapter's data set of the item whose view we want.
     * convertView	The old view to reuse, if possible. Note: You should check that this view is non-null and of an appropriate type before using. If it is not possible to convert this view to display the correct data, this method can create a new view. Heterogeneous lists can specify their number of view types, so that this View is always of the right type (see getViewTypeCount() and getItemViewType(int)).
     * parent	The parent that this view will eventually be attached to
     * Returns
     * A View corresponding to the data at the specified position.
     */


	/*
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.itemlista, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.item_title);
		textView.setText(items.get(position).getTitle());
	    return rowView;
	}
	/**/

    //http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        TextView item_title;
        TextView item_date;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(getContext(), linkResource, null);
            holder = new ViewHolder();
            holder.item_title = (TextView) convertView.findViewById(R.id.item_title);
            holder.item_date = (TextView) convertView.findViewById(R.id.item_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
       // String pubdate = cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_DATE));
        //String title = cursor.getString(cursor.getColumnIndexOrThrow(PodcastDBHelper.EPISODE_TITLE));

        holder.item_title.setText(getItem(position).getTitle());
        //passo 5
        holder.item_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c,EpisodeDetailActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//essa flag é necessária pois o listener está sendo instanciado
                //no próprio adapter e não numa activity
                i.putExtra("Titulo",getItem(position).getTitle());
                i.putExtra("Descricao",getItem(position).getDescription());
                i.putExtra("PubDate",getItem(position).getPubDate());
                c.startActivity(i);//redireciona para a activity desejada
            }
        });
        holder.item_date.setText(getItem(position).getPubDate());
        return convertView;
    }
}