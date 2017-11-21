package com.yo.android.ui.followmoretopics;


import com.yo.android.model.Topics;
import com.yo.android.widgets.expandablerecycler.ExpandableItem;
import java.util.ArrayList;
import java.util.List;

public class CategoriesAccordionSection extends ExpandableItem<ArrayList<Topics>> {

    private String id;
    private String name;
    private boolean language_specific;
    private ArrayList<Topics> tags;
    private boolean isExpandable;
    private boolean isLoading;
    private boolean isSelectedAll;

    public CategoriesAccordionSection(String id, String name, boolean language_specific, ArrayList<Topics> tags, boolean isExpandable) {
        this.id = id;
        this.name = name;
        this.language_specific = language_specific;
        this.tags = tags;
        this.isExpandable = isExpandable;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isLanguage_specific() {
        return language_specific;
    }

    public List<Topics> getTags() {
        return tags;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isSelectedAll() {
        return isSelectedAll;
    }

    public void setSelectedAll(boolean selectedAll) {
        isSelectedAll = selectedAll;
    }

    @Override
    public ArrayList<Topics> getChild() {
        return tags;
    }

    public boolean checkAllTopicsSelected() {
        int selectedCount = 0;
        if(getChild() != null && getChild().size() > 0) {
            for (Topics topics :getChild()) {
                if(topics.isSelected()) {
                    selectedCount++;
                }
            }

            if(selectedCount == getChild().size()) {
                setSelectedAll(true);
            } else {
                setSelectedAll(false);
            }
        }

        return isSelectedAll();
    }

    public void checkAllTopics(ArrayList<String> selectedTopics) {
        if(getChild() != null && getChild().size() > 0) {
            for (Topics topics :getChild()) {
                topics.setSelected(true);
                selectedTopics.add(topics.getId());
            }
        }
    }

    public void unCheckAllTopics(ArrayList<String> selectedTopics) {
        if(getChild() != null && getChild().size() > 0) {
            for (Topics topics :getChild()) {
                topics.setSelected(false);
                selectedTopics.remove(topics.getId());
            }
        }
    }
}
