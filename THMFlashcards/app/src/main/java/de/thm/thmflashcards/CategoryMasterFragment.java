package de.thm.thmflashcards;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import de.thm.thmflashcards.persistance.Category;
import de.thm.thmflashcards.persistance.AppDatabase;
import de.thm.thmflashcards.persistance.SubCategory;

/**
 * Created by Yannick Bals on 09.11.2017.
 */

public class CategoryMasterFragment extends Fragment {

    private boolean isDualView;

    private List<Category> categories;
    private HashMap<Integer, List<SubCategory>> categorieItems;

    private CategoryListAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        categories = new ArrayList<>();
        categorieItems = new HashMap<>();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        //Inflate the view
        View view = inflater.inflate(R.layout.category_master, container, false);

        //Get the FAB and attach a listener
        FloatingActionButton fab = view.findViewById(R.id.addCategoryButton);
        fab.setOnClickListener(new FABListener());

        //Set up the expandable List View
        ExpandableListView categoryView = view.findViewById(R.id.categoryView);
        adapter = new CategoryListAdapter(getActivity(), categories, categorieItems);
        categoryView.setAdapter(adapter);
        categoryView.setLongClickable(true);
        categoryView.setOnChildClickListener(new CategoryChildOnClickListener());
        categoryView.setOnItemLongClickListener(new LongClickListener());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Check if the Activity layout contains the detail view. If it does, we need to handle clicks differently.
        View detailsFrame = getActivity().findViewById(R.id.detailContainer);
        isDualView = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

    }

    @Override
    public void onResume() {
        super.onResume();

        loadCategories();

    }

    /**
     * Loads categories from the internal database
     */
    private void loadCategories() {
        new LoadCategoriesTask().execute();
    }

    /**
     * Loads subcategories from the internal database
     */
    private void loadSubCategories() {
        new LoadSubCategoriesTask().execute(categories);
    }

    /**
     * Give the updated Lists to the adapter and let him update the list
     */
    private void updateList() {
        adapter.updateData(categories, categorieItems);
    }

    /**
     * Listener for the Floating Action Button
     */
    class FABListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            //Build a dialog to enter the new category
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getResources().getString(R.string.category_dialog_title));

            //Set up the text input
            View dialogContent = LayoutInflater.from(getActivity()).inflate(R.layout.text_input_dialog, null);
            final AppCompatEditText categoryEditText = dialogContent.findViewById(R.id.dialogInput);
            categoryEditText.setHint(getResources().getString(R.string.category_dialog_hint));

            //Attach the input to the dialog view
            builder.setView(dialogContent);

            //Set up the buttons
            builder.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String categoryName = categoryEditText.getText().toString();
                    Category category = new Category();
                    category.setName(categoryName);
                    new InsertCategoryTask().execute(category);
                }
            });

            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            //Show the dialog
            builder.show();

        }
    }

    /**
     * Handle clicks on the child items of the expandable list
     */
    class CategoryChildOnClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
            return false;
        }
    }

    /**
     * Handle long clicks on the expandable list
     */
    class LongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            int itemType = ExpandableListView.getPackedPositionType(id);

            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                Log.e("Long click", "GROUP");
                return true;
            } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
                Log.e("Long click", "CHILD");
                return true;
            } else {
                return false;
            }

        }
    }

    /**
     * AsyncTask to load the category data
     */
    class LoadCategoriesTask extends AsyncTask<Void, Void, List<Category>> {
        @Override
        protected List<Category> doInBackground(Void... voids) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            List<Category> categories = new ArrayList<>();
            Category[] categoriesArray = db.categoryDao().loadAllCategories();
            if (categoriesArray != null && categoriesArray.length > 0) {
                for (Category category: categoriesArray) {
                    categories.add(category);
                }
            }
            return categories;
        }

        @Override
        protected void onPostExecute(List<Category> categories) {
            CategoryMasterFragment.this.categories = categories;
            loadSubCategories();
        }
    }

    /**
     * AsyncTask to load the category data
     */
    class LoadSubCategoriesTask extends AsyncTask<List<Category>, Void, HashMap<Integer, List<SubCategory>>> {

        @Override
        protected HashMap<Integer, List<SubCategory>> doInBackground(List<Category>[] lists) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            List<Category> categories = lists[0];
            HashMap<Integer, List<SubCategory>> subCategories = new HashMap<>();
            for (Category category : categories) {
                List<SubCategory> subCategoryList = new ArrayList<>();
                SubCategory[] subArray = db.subCategoryDao().loadSubCategoriesOfCategory(category.getId());
                if (subArray != null && subArray.length > 0) {
                    for (SubCategory subCategory : subArray) {
                        subCategoryList.add(subCategory);
                    }
                    subCategories.put(category.getId(), subCategoryList);
                }
            }

            return subCategories;
        }

        @Override
        protected void onPostExecute(HashMap<Integer, List<SubCategory>> integerListHashMap) {
            categorieItems = integerListHashMap;
            updateList();
        }
    }

    /**
     * AsyncTask to insert a new category
     */
    class InsertCategoryTask extends AsyncTask<Category, Void, Long> {
        @Override
        protected Long doInBackground(Category... categories) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            return db.categoryDao().insertCategory(categories[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong != null) {
                loadCategories();
            }
        }
    }
}
