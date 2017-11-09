package de.thm.thmflashcards;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Farea on 09.11.2017.
 */

public class CategoryMasterFragment extends Fragment {

    private boolean isDualView;

    private List<String> categories;
    private HashMap<String, List<String>> categorieItems;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Items for testing
        categories = new ArrayList<>();
        categories.add("QM");
        categories.add("SAG");
        categories.add("Empty");

        categorieItems = new HashMap<>();

        List<String> QMItems = new ArrayList<>();
        QMItems.add("Häufigkeiten");
        QMItems.add("Varianz");
        QMItems.add("Korrelation");

        List<String> SAGItems = new ArrayList<>();
        SAGItems.add("State Pattern");

        List<String> emptyTest = new ArrayList<>();

        categorieItems.put(categories.get(0), QMItems);
        categorieItems.put(categories.get(1), SAGItems);
        categorieItems.put(categories.get(2), emptyTest);
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
        CategoryListAdapter adapter = new CategoryListAdapter(getActivity(), categories, categorieItems);
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
                    String category = categoryEditText.getText().toString();
                    Toast.makeText(getActivity(), category, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getActivity(), categorieItems.get(categories.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
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
}
