package com.yo.android.sectionheaders;

/**
 * Created by creatives on 12/20/2016.
 */
/// <summary>
/// Section.
/// </summary>
public class Section
{
    /// <summary>
    /// Gets or sets the layout identifier.
    /// </summary>
    /// <value>The layout identifier.</value>
    private int layoutId;

    /// <summary>
    /// Gets or sets the section item.
    /// </summary>
    /// <value>The section item.</value>
    private SectionItem sectionItem;

    /// <summary>
    /// Gets or sets the section header.
    /// </summary>
    /// <value>The section header.</value>
    private String sectionHeader;

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public SectionItem getSectionItem() {
        return sectionItem;
    }

    public void setSectionItem(SectionItem sectionItem) {
        this.sectionItem = sectionItem;
    }

    public String getSectionHeader() {
        return sectionHeader;
    }

    public void setSectionHeader(String sectionHeader) {
        this.sectionHeader = sectionHeader;
    }
}
