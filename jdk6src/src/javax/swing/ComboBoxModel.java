/*
 * @(#)ComboBoxModel.java	1.16 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package javax.swing;

/**
 * A data model for a combo box. This interface extends <code>ListDataModel</code>
 * and adds the concept of a <i>selected item</i>. The selected item is generally
 * the item which is visible in the combo box display area.
 * <p>
 * The selected item may not necessarily be managed by the underlying 
 * <code>ListModel</code>. This disjoint behavior allows for the temporary 
 * storage and retrieval of a selected item in the model.
 *
 * @version 1.16 11/17/05
 * @author Arnaud Weber
 */
public interface ComboBoxModel extends ListModel {

  /** 
   * Set the selected item. The implementation of this  method should notify 
   * all registered <code>ListDataListener</code>s that the contents 
   * have changed. 
   * 
   * @param anItem the list object to select or <code>null</code> 
   *        to clear the selection
   */
  void setSelectedItem(Object anItem);

  /** 
   * Returns the selected item 
   * @return The selected item or <code>null</code> if there is no selection
   */
  Object getSelectedItem();
}

