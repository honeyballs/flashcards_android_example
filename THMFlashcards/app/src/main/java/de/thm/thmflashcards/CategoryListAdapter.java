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

import de.thm.thmflashcards.persistance.Category;
import de.thm.thmflashcards.persistance.SubCategory;

/**
 * Created by Yannick Bals on 07.11.2017.
 */

public class CategoryListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Category> categories;
    private HashMap<Integer, List<SubCategory>> subCategories;

    public CategoryListAdapter(Context c, List<Category> categories, HashMap<Integer, List<SubCategory>> children) {
        this.context = c;
        this.categories = categories;
        this.subCategories = children;
    }

    @Override
    public int getGroupCount() {
        return categories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (subCategories.containsKey(categories.get(groupPosition).getId())) {
            return subCategories.get(categories.get(groupPosition).getId()).size();
        } else {
            return 0;
        }
    }

    @Override
    public Category getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    @Override
    public SubCategory getChild(int groupPosition, int childPosition) {
        return subCategories.get(categories.get(groupPosition).getId()).get(childPosition);
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
        String category = getGroup(groupPosition).getName();
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
        String categoryItem = getChild(groupPosition, childPosition).getName();
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

    //Define Methods to add/remove items to and from the lists in this adapter to get instant updates
    public void updateData(List<Category> categories, HashMap<Integer, List<SubCategory>> subCategories) {
        this.categories = categories;
        this.subCategories = subCategories;
        notifyDataSetChanged();
    }

    public void addCategory(Category category) {
        categories.add(category);
        notifyDataSetChanged();
    }

    public void addSubCategories(int id, List<SubCategory> subCategoryList) {
        subCategories.put(id, subCategoryList);
        notifyDataSetChanged();
    }

    public void clearCategories() {
        subCategories.clear();
        categories.clear();
        notifyDataSetChanged();
    }

    public void clearSubCategories() {
        subCategories.clear();
        notifyDataSetChanged();
    }
}
