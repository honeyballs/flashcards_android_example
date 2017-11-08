package de.thm.thmflashcards;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Farea on 07.11.2017.
 */

public class CategoryListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> categoryHeaders;
    private HashMap<String, List<String>> categoryChildren;

    public CategoryListAdapter(Context c, List<String> categories, HashMap<String, List<String>> children) {
        this.context = c;
        this.categoryHeaders = categories;
        this.categoryChildren = children;
    }

    @Override
    public int getGroupCount() {
        return categoryHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return categoryChildren.get(categoryHeaders.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryChildren.get(categoryHeaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
        String category = (String) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.category_header, null);
        }
        TextView headerView = view.findViewById(R.id.categoryTextView);
        headerView.setTypeface(null, Typeface.BOLD);
        headerView.setText(category);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
        String categoryItem = (String) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.category_item, null);
        }
        TextView itemView = view.findViewById(R.id.categoryItemTextView);
        itemView.setText(categoryItem);
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
