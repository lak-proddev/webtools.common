/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.common.ui.internal.search.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.wst.common.core.search.scope.ProjectSearchScope;
import org.eclipse.wst.common.core.search.scope.SearchScope;
import org.eclipse.wst.common.core.search.scope.WorkingSetSearchScope;
import org.eclipse.wst.common.core.search.scope.WorkspaceSearchScope;
import org.eclipse.wst.common.ui.internal.UIPlugin;

public class ScopedComponentSearchListDialog extends ComponentSearchListDialog
{
  protected final static String DEFAULT_NAME_FIELD_TITLE = UIPlugin.getString("_UI_LABEL_COMPONENT_NAME");
  protected final static String DEFAULT_LIST_TITLE = UIPlugin.getString("_UI_LABEL_MATCHING_COMPONENTS");
  public static final String SCOPE_SPECIFIED_FILE = UIPlugin.getString("_UI_LABEL_SPECIFIED_FILE");
  public static final String SCOPE_ENCLOSING_PROJECT = UIPlugin.getString("_UI_LABEL_ENCLOSING_PROJECT");
  public static final String SCOPE_WORKSPACE = UIPlugin.getString("_UI_LABEL_WORKSPACE");
  public static final String SCOPE_CURRENT_RESOURCE = UIPlugin.getString("_UI_LABEL_CURRENT_RESOURCE");
  //TODO externalize to UIPlugin
  public static final String SCOPE_WORKING_SETS = "Working Sets";
  
  private String currentSearchScope = SCOPE_CURRENT_RESOURCE;
  protected Button chooseButton;
  protected Button[] radioButton = new Button[4];
  protected String filterLabel;
  protected String listTitle;
  protected IResource currentResource;
  protected Composite selectWorkingSetsGroup;
  protected Text workingSetsText;
  
  // working sets currently chosen by the user
  private IWorkingSet[] workingSets;

  public ScopedComponentSearchListDialog(Shell shell, String dialogTitle, ComponentSearchListDialogConfiguration configuration)
  {
    super(shell, dialogTitle, configuration);
  }
  
  public void setFilterLabel(String filterLabel)
  {
    this.filterLabel = filterLabel;
  }
  
  public void setListTitle(String listTitle)
  {
    this.listTitle = listTitle;
  }  

  protected Control createDialogArea(Composite parent)
  {
    //super.setFilterLabel(filterLabel != null ? filterLabel : DEFAULT_NAME_FIELD_TITLE);
    //setListLabelText(listTitle != null ? listTitle : DEFAULT_LIST_TITLE);
    super.createDialogArea(parent);
    // We use the Composite topComposite to create additional widgets
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    bottomComposite.setLayout(layout);
    Group group = new Group(bottomComposite, SWT.NONE);
    GridLayout gridLayout = new GridLayout(3, false);
    // gridLayout.marginWidth = 0;
    // gridLayout.marginLeft = 2;
    group.setLayout(gridLayout);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setText(UIPlugin.getString("_UI_LABEL_SEARCH_SCOPE"));
    ScopeChangeListener scopeChangeListener = new ScopeChangeListener();
    radioButton[0] = new Button(group, SWT.RADIO);
    radioButton[0].setText(SCOPE_WORKSPACE);
    radioButton[1] = new Button(group, SWT.RADIO);
    radioButton[1].setText(SCOPE_ENCLOSING_PROJECT);
    radioButton[2] = new Button(group, SWT.RADIO);
    radioButton[2].setText(SCOPE_CURRENT_RESOURCE);
    radioButton[3] = new Button(group, SWT.RADIO);
    radioButton[3].setText(SCOPE_WORKING_SETS);
    for (int i = 0; i < radioButton.length; i++)
    {
      if (radioButton[i].getText().equals(currentSearchScope))
      {
        radioButton[i].setSelection(true);
      }
      radioButton[i].addSelectionListener(scopeChangeListener);
    }
    
    selectWorkingSetsGroup = new Composite(group, SWT.NONE);
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    selectWorkingSetsGroup.setLayoutData(gd);
    GridLayout workingSetGroupLayout = new GridLayout(2, false);
    workingSetGroupLayout.marginWidth = 0;
    workingSetGroupLayout.marginHeight = 0;
    selectWorkingSetsGroup.setLayout(workingSetGroupLayout);
    
    workingSetsText = new Text(selectWorkingSetsGroup, SWT.BORDER | SWT.READ_ONLY);
    workingSetsText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    
    chooseButton = new Button(selectWorkingSetsGroup, SWT.NONE);
    chooseButton.setText("Choose...");
    chooseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Shell shell = getShell();
			IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(shell, true);
			if ( dialog.open() == Window.OK){
				workingSets = dialog.getSelection();
				String names = "";
				for (int i = 0; i < workingSets.length; i++){
					names += workingSets[i].getLabel();
					// if not the last item, we add a comma
					if ( i != workingSets.length - 1) 
						names += ", ";
				}
				workingSetsText.setText(names);
			}
			// Set working sets radio button enabled, disable other buttons
			radioButton[2].setSelection(false);
			radioButton[1].setSelection(false);
			radioButton[0].setSelection(false);
			radioButton[3].setSelection(true);

			scopeChangeHandler(radioButton[3]);
		}
    });

    return parent;
  } /*
     * Returns the selected search scope.
     */

  public String getSearchScope()
  {
    return currentSearchScope;
  }

private void scopeChangeHandler(Button b) {	  
	  if ((b.getStyle() & SWT.RADIO) != 0)
	  {
		  currentSearchScope = b.getText();
		  
		  // TODO (cs) need to do some work to make the default search scope
		  // more well defined, currently the default behaviour is to pass a null
		  // argument in to populateMasterComponentList but we should provide
		  // getters/setters to allow the default to be controlled          
		  SearchScope scope = null;
		  if (currentSearchScope == SCOPE_ENCLOSING_PROJECT &&
				  currentResource != null) { 
			  scope = new ProjectSearchScope(currentResource.getFullPath());
		  } 
		  else if (currentSearchScope == SCOPE_WORKSPACE){
			  scope = new WorkspaceSearchScope();
		  }
		  else if (currentSearchScope == SCOPE_WORKING_SETS){
			  
			  // Constructs the working sets scope from the working sets the user
			  // selected
			  WorkingSetSearchScope workingSetsScope = new WorkingSetSearchScope();
			  for (int i = 0; i < workingSets.length; i++){
				  workingSetsScope.addAWorkingSetToScope(workingSets[i].getElements());
			  }			  
			  scope = workingSetsScope;
		  }
		  
		  populateMasterComponentList(scope);
		  refreshTableViewer(getProcessedFilterString());
		  // Select the first matching component. Though we should be
		  // smarter here
		  // and determine if there was a selection before the scope
		  // switch (and if
		  // the component is still available.
		  Table table = componentTableViewer.getTable();
		  TableItem items[] = table.getItems();
		  if (items.length > 0)
		  {
			  TableItem select[] = new TableItem[1];
			  select[0] = items[0];
			  table.setSelection(select);
		  }
		  updateCanFinish();
	  }
  }

private class ScopeChangeListener extends SelectionAdapter
  {
    public void widgetSelected(SelectionEvent e)
    {
      if (e.widget instanceof Button)
      {
    	Button b = (Button) e.widget;
        scopeChangeHandler(b);
      }
    }
  }

  public IResource getCurrentResource()
  {
    return currentResource;
  }

  public void setCurrentResource(IResource currentResource)
  {
    this.currentResource = currentResource;
  }
}