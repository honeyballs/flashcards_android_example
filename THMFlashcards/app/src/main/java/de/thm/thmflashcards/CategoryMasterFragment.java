package de.thm.thmflashcards;

import android.app.ExpandableListActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
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

    private Communicator communicator;

    private List<Category> categories;
    private HashMap<Integer, List<SubCategory>> categorieItems;

    private ExpandableListView categoryView;
    private CategoryListAdapter adapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //Cast the context (parent activity) to the communicator interface
        try {
            communicator = (Communicator) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

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
        categoryView = view.findViewById(R.id.categoryView);
        adapter = new CategoryListAdapter(getActivity(), categories, categorieItems);
        categoryView.setAdapter(adapter);
        categoryView.setLongClickable(true);
        categoryView.setOnChildClickListener(new CategoryChildOnClickListener());
        categoryView.setOnItemLongClickListener(new LongClickListener());

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        categories.clear();
        categorieItems.clear();

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

            final SubCategory subCategory = (SubCategory) expandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition);
            final Category category = (Category) expandableListView.getExpandableListAdapter().getGroup(groupPosition);

            //If this is the add item we display the dialog to add data
            if (subCategory.getName().equals("Add")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.subcategory_dialog_title));
                View dialogContent = LayoutInflater.from(getActivity()).inflate(R.layout.text_input_dialog, null);
                final AppCompatEditText categoryEditText = dialogContent.findViewById(R.id.dialogInput);
                categoryEditText.setHint(getResources().getString(R.string.subcategory_dialog_hint));
                builder.setView(dialogContent);

                //Set up the buttons
                builder.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String subCategoryName = categoryEditText.getText().toString();

                        //Check if someone typed "Add" since this is used to identify the item to add subcategories
                        if (subCategoryName.equals("Add")) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.add_typed), Toast.LENGTH_SHORT).show();
                        } else {
                            SubCategory newSubCategory = new SubCategory();
                            newSubCategory.setName(subCategoryName);
                            //Fulfill the foreign key constraint
                            newSubCategory.setCategoryId(category.getId());
                            new InsertSubCategoryTask().execute(newSubCategory);
                        }

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
            } else {
                //Let the activity start/show the cards
                communicator.loadDetailFor(subCategory.getId());
            }

            return false;
        }
    }

    /**
     * Handle long clicks on the expandable list
     */
    class LongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            //Used to check whether we are clicking on a subcategory or a category
            int itemType = ExpandableListView.getPackedPositionType(id);
            //The id we receive here is a packed value which consists of both positions (group and child), so we have to unpack it
            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
            int childPosition = ExpandableListView.getPackedPositionChild(id);

            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                final Category category = (Category) categoryView.getExpandableListAdapter().getGroup(groupPosition);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.delete_category_title));
                builder.setMessage(getResources().getString(R.string.delete_category));
                builder.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteCategoryTask().execute(category);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return true;
            } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD){
                final SubCategory subCategory = (SubCategory) categoryView.getExpandableListAdapter().getChild(groupPosition, childPosition);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.delete_subcategory_title));
                builder.setMessage(getResources().getString(R.string.delete_subcategory));
                builder.setPositiveButton(getResources().getString(R.string.Ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteSubCategoryTask().execute(subCategory);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
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
                }
                subCategories.put(category.getId(), subCategoryList);
            }

            return subCategories;
        }

        @Override
        protected void onPostExecute(HashMap<Integer, List<SubCategory>> integerListHashMap) {

            //Add an item that is used to add new subcategories to each category
            for (Integer id : integerListHashMap.keySet()) {
                SubCategory addSub = new SubCategory();
                addSub.setName("Add");
                integerListHashMap.get(id).add(addSub);
            }

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

    class InsertSubCategoryTask extends AsyncTask<SubCategory, Void, Long> {
        @Override
        protected Long doInBackground(SubCategory... subCategories) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            return db.subCategoryDao().insertSubCategory(subCategories[0]);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (aLong != null) {
                loadSubCategories();
            }
        }
    }

    class DeleteCategoryTask extends AsyncTask<Category, Void, Integer> {

        @Override
        protected Integer doInBackground(Category... categories) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            return db.categoryDao().deleteCategory(categories[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Log.e("Rows deleted: ", "" + integer);
            loadCategories();
        }
    }

    class DeleteSubCategoryTask extends AsyncTask<SubCategory, Void, Integer> {

        @Override
        protected Integer doInBackground(SubCategory... subCategories) {
            AppDatabase db = AppDatabase.getAppDataBase(getActivity());
            return db.subCategoryDao().deleteSubCategoty(subCategories[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            Log.e("Rows deleted: ", "" + integer);
            loadSubCategories();
        }
    }
}
