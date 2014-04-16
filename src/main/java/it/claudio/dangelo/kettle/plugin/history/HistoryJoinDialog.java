package it.claudio.dangelo.kettle.plugin.history;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;


public class HistoryJoinDialog extends BaseStepDialog implements StepDialogInterface
{
	private static Class<?> PKG = HistoryJoinMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CCombo       wConnection;

	private Label        wlParam;
	private TableView    wParam;
	private FormData     fdlParam, fdParam;
	
	private Button wGet;
	private Listener lsGet;

	private HistoryJoinMeta input;
	
	private Label        wlPosition;
	private FormData     fdlPosition;
	
	private SQLValuesHighlight lineStyler = new SQLValuesHighlight();
	
	private ColumnInfo[] ciKey;
	
    private Map<String, Integer> inputFields;
    
    private Composite sc;
	private ModifyListener lsMod;
    

	public HistoryJoinDialog(Shell parent, Object in, TransMeta tr, String sname)
	{
		super(parent, (BaseStepMeta)in, tr, sname);
		input=(HistoryJoinMeta)in;
	    inputFields =new HashMap<String, Integer>();
	}
	@Override
	public String open() {
		Shell parent = getParent();
		Display display = parent.getDisplay();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.MIN);
		shell.setLayout(new FillLayout(SWT.VERTICAL));
		shell.setText("Date dimension output"); //$NON-NLS-1$
		final ScrolledComposite scrollComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		//sc = shell;
		//sc = new Composite(shell, SWT.V_SCROLL);
		sc = new Composite(scrollComposite, SWT.NONE);
		//props.setLook(sc);
		
		props.setLook(shell);
		setShellImage(shell, input);
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		sc.setLayout(formLayout);

		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				input.setChanged();
			}
		};
		this.addStepName(middle, margin);
		// connection line
		wConnection = addConnectionLine(sc, wStepname, middle, margin);
		if (input.getDatabaseMeta()==null && transMeta.nrDatabases()==1) 
			wConnection.select(0);
		wConnection.addModifyListener(lsMod);
		
		Control topControl = wConnection;

		topControl = this.addHistoryVarInputs(middle, margin, topControl);
		topControl = this.addHistoryColumns(middle, margin, topControl);
		topControl = this.addSQLText(middle, margin, topControl);
//		topControl = this.addLimit(middle, margin, topControl);
//		topControl = this.addOuterJoin(middle, margin, topControl);
		topControl = this.addAddType(middle, margin, topControl);
		topControl = this.addUseVars(middle, margin, topControl);
		topControl = this.addParameters(middle, margin, topControl);
		this.addButtons(middle, margin, topControl);

		
		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		
		wStepname.addSelectionListener( lsDef );
//		wLimit.addSelectionListener( lsDef );
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
				
		getData();
			
		scrollComposite.setContent(sc);
		scrollComposite.setExpandVertical(true);
		scrollComposite.setExpandHorizontal(true);
		final Composite composite = sc;
		scrollComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle r = scrollComposite.getClientArea();
				scrollComposite.setMinSize(composite.computeSize(r.width, SWT.DEFAULT));
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}
	
	private Control addStepName(int middle, int margin) {
		// Stepname line
		wlStepname = new Label(sc, SWT.RIGHT);
		wlStepname.setText("Step Name:"); //$NON-NLS-1$
		props.setLook(wlStepname);
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right = new FormAttachment(middle, -margin);
		fdlStepname.top = new FormAttachment(0, margin);
		wlStepname.setLayoutData(fdlStepname);
		wStepname = new Text(sc, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		props.setLook(wStepname);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(middle, 0);
		fdStepname.top = new FormAttachment(0, margin);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		return wStepname;
	}
	
	private Control addButtons(int middle, int margin, Control topControl) {
		// THE BUTTONS
		wOK=new Button(sc, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wGet=new Button(sc, SWT.PUSH);
		wGet.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.GetFields.Button")); //$NON-NLS-1$
		wCancel=new Button(sc, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		BaseStepDialog.positionBottomButtons(sc, new Button[] { wOK, wCancel , wGet}, margin, topControl);

		
		lsOK       = new Listener() { public void handleEvent(Event e) { ok();        } };
		lsGet      = new Listener() { public void handleEvent(Event e) { get();       } };
		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel();    } };
		
		wOK.addListener    (SWT.Selection, lsOK    );
		wGet.addListener   (SWT.Selection, lsGet   );
		wCancel.addListener(SWT.Selection, lsCancel);
		return wOK;
	}
	

	private Control addParameters(int middle, int margin, Control topControl) {
		// The parameters
		wlParam=new Label(sc, SWT.NONE);
		wlParam.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.Param.Label")); //$NON-NLS-1$
 		props.setLook(wlParam);
		fdlParam=new FormData();
		fdlParam.left  = new FormAttachment(0, 0);
		fdlParam.top   = new FormAttachment(topControl, margin);
		wlParam.setLayoutData(fdlParam);

		int nrKeyCols=2;
		int nrKeyRows=(input.getParameterField()!=null?input.getParameterField().length:1);
		
		ciKey=new ColumnInfo[nrKeyCols];
		ciKey[0]=new ColumnInfo(BaseMessages.getString(PKG, "DatabaseJoinDialog.ColumnInfo.ParameterFieldname"),  ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }, false);
		ciKey[1]=new ColumnInfo(BaseMessages.getString(PKG, "DatabaseJoinDialog.ColumnInfo.ParameterType"),       ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes() ); //$NON-NLS-1$
		
		wParam=new TableView(transMeta, sc, 
						      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, 
						      ciKey, 
						      nrKeyRows,  
						      lsMod,
							  props
						      );

		fdParam=new FormData();
		fdParam.left  = new FormAttachment(0, 0);
		fdParam.top   = new FormAttachment(wlParam, margin);
		fdParam.right = new FormAttachment(100, 0);
//		fdParam.bottom= new FormAttachment(wOK, -2*margin);
		wParam.setLayoutData(fdParam);

		  // 
        // Search the fields in the background
		
        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                StepMeta stepMeta = transMeta.findStep(stepname);
                if (stepMeta!=null)
                {
                    try
                    {
                    	RowMetaInterface row = transMeta.getPrevStepFields(stepMeta);
                       
                        // Remember these fields...
                        for (int i=0;i<row.size();i++)
                        {
                            inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                        }
//                        setComboBoxes();
                        ciKey[0].setComboValues(getFields());
                    }
                    catch(KettleException e)
                    {
                    	logError(BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                    }
                }
            }
        };
        new Thread(runnable).start();
        return wParam;
	}
	
	private Label        wluseVars;
	private Button       wuseVars;
	private FormData     fdluseVars, fduseVars;

	private Control addUseVars(int middle, int margin, Control topControl) {
		// useVars ?
		wluseVars=new Label(sc, SWT.RIGHT);
		wluseVars.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.useVarsjoin.Label")); //$NON-NLS-1$
		wluseVars.setToolTipText(BaseMessages.getString(PKG, "DatabaseJoinDialog.useVarsjoin.Tooltip")); //$NON-NLS-1$
			props.setLook(wluseVars);
		fdluseVars=new FormData();
		fdluseVars.left = new FormAttachment(0, 0);
		fdluseVars.right= new FormAttachment(middle, -margin);
		fdluseVars.top  = new FormAttachment(topControl, margin);
		wluseVars.setLayoutData(fdluseVars);
		wuseVars=new Button(sc, SWT.CHECK);
			props.setLook(wuseVars);
		wuseVars.setToolTipText(wluseVars.getToolTipText());
		fduseVars=new FormData();
		fduseVars.left = new FormAttachment(middle, 0);
		fduseVars.top  = new FormAttachment(topControl, margin);
		wuseVars.setLayoutData(fduseVars);
		wuseVars.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		return wuseVars;
	}
	/*
	
	private Label        wlOuter;
	private Button       wOuter;
	private FormData     fdlOuter, fdOuter;

	private Control addOuterJoin(int middle, int margin, Control topControl)  {
		// Outer join?
		wlOuter=new Label(sc, SWT.RIGHT);
		wlOuter.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.Outerjoin.Label")); //$NON-NLS-1$
		wlOuter.setToolTipText(BaseMessages.getString(PKG, "DatabaseJoinDialog.Outerjoin.Tooltip")); //$NON-NLS-1$
 		props.setLook(wlOuter);
		fdlOuter=new FormData();
		fdlOuter.left = new FormAttachment(0, 0);
		fdlOuter.right= new FormAttachment(middle, -margin);
		fdlOuter.top  = new FormAttachment(topControl, margin);
		wlOuter.setLayoutData(fdlOuter);
		wOuter=new Button(sc, SWT.CHECK);
 		props.setLook(wOuter);
		wOuter.setToolTipText(wlOuter.getToolTipText());
		fdOuter=new FormData();
		fdOuter.left = new FormAttachment(middle, 0);
		fdOuter.top  = new FormAttachment(topControl, margin);
		wOuter.setLayoutData(fdOuter);
		wOuter.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e) 
				{
					input.setChanged();
				}
			}
		);
		return wOuter;
	}
	
	
	private Label        wlLimit;
	private Text         wLimit;
	private FormData     fdlLimit, fdLimit;

	private Control addLimit(int middle, int margin, Control topControl) {
		// Limit the number of lines returns
		wlLimit=new Label(sc, SWT.RIGHT);
		wlLimit.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.Limit.Label")); //$NON-NLS-1$
 		props.setLook(wlLimit);
		fdlLimit=new FormData();
		fdlLimit.left   = new FormAttachment(0, 0);
		fdlLimit.right  = new FormAttachment(middle, -margin);
		fdlLimit.top    = new FormAttachment(topControl, margin);
		wlLimit.setLayoutData(fdlLimit);
		wLimit=new Text(sc, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		props.setLook(wLimit);
		wLimit.addModifyListener(lsMod);
		fdLimit=new FormData();
		fdLimit.left   = new FormAttachment(middle, 0);
		fdLimit.right  = new FormAttachment(100, 0);
		fdLimit.top    = new FormAttachment(topControl, margin);
		wLimit.setLayoutData(fdLimit);
		return wLimit;
	}
	*/ 
	
	private Label wlAddType;
	private CCombo wAddType;
	private FormData fdlAddType,fdAddType;
	
	private Control addAddType(int middle, int margin, Control topControl) {
		wlAddType = new Label(sc, SWT.NONE);
		wlAddType.setText("Add type : ");
		props.setLook(wlAddType);
		fdlAddType = new FormData();
		fdlAddType.left = new FormAttachment(0,0);
		fdlAddType.top = new FormAttachment(topControl, margin *2);
		wlAddType.setLayoutData(fdlAddType);
		
		wAddType = new CCombo(sc, SWT.NONE);
		AddTypes[] types = AddTypes.values();
		for (AddTypes addTypes : types) {
			wAddType.add(addTypes.toString());
		}
		fdAddType = new FormData();
		fdAddType.left = new FormAttachment(wlAddType, margin*2);
		fdAddType.top = new FormAttachment(topControl, margin*2);
		wAddType.setLayoutData(fdAddType);
		return wAddType;
	}

	private Label        wlSQL;
	private StyledTextComp   wSQL;
	private FormData     fdlSQL, fdSQL;

	private Control addSQLText(int middle, int margin, Control topControl) {
		// SQL field
		// SQL editor...
		wlSQL=new Label(sc, SWT.NONE);
		wlSQL.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.SQL.Label")); //$NON-NLS-1$
 		props.setLook(wlSQL);
		fdlSQL=new FormData();
		fdlSQL.left = new FormAttachment(0, 0);
		fdlSQL.top  = new FormAttachment(topControl, margin*2);
		wlSQL.setLayoutData(fdlSQL);

		wSQL=new StyledTextComp(variables, sc, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
 		props.setLook(wSQL, Props.WIDGET_STYLE_FIXED);
		wSQL.addModifyListener(lsMod);
		fdSQL=new FormData();
		fdSQL.left  = new FormAttachment(0, 0);
		fdSQL.top   = new FormAttachment(wlSQL, margin  );
		fdSQL.right = new FormAttachment(100, 0);
		fdSQL.bottom= new FormAttachment(60, 0     );
		wSQL.setLayoutData(fdSQL);
		
		
		wSQL.addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent arg0)
            {
                setPosition();
            }

	        }
	    );
			
		
		wSQL.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) { setPosition(); }
			public void keyReleased(KeyEvent e) { setPosition(); }
			} 
		);
		wSQL.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) { setPosition(); }
			public void focusLost(FocusEvent e) { setPosition(); }
			}
		);
		wSQL.addMouseListener(new MouseAdapter(){
			public void mouseDoubleClick(MouseEvent e) { setPosition(); }
			public void mouseDown(MouseEvent e) { setPosition(); }
			public void mouseUp(MouseEvent e) { setPosition(); }
			}
		);
		
		// SQL Higlighting
		lineStyler = new SQLValuesHighlight();;
		wSQL.addLineStyleListener(lineStyler);
		
		wlPosition=new Label(sc, SWT.NONE);
		props.setLook(wlPosition);
		fdlPosition=new FormData();
		fdlPosition.left  = new FormAttachment(0,0);
		fdlPosition.top = new FormAttachment(wSQL, margin);
		fdlPosition.right = new FormAttachment(100, 0);
		wlPosition.setLayoutData(fdlPosition);

		return wlPosition;
	}
	
	private String[] getFields() {
		RowMetaInterface row;
		try {
			row = transMeta.getPrevStepFields(stepMeta);
		} catch (KettleStepException e) {
			throw new RuntimeException(e);
		}
		String[] results = new String[row.size()];
		for (int i=0;i<row.size();i++)
			results[i] = row.getValueMeta(i).getName();
		Arrays.sort(results);
		return results;
	}
	
	private void addFieldToCombo(CCombo combo)  {
		String[] fields = this.getFields();
		for (String string : fields) {
			combo.add(string);
		}
	}

	private Label wlhistoryDates;
	private CCombo  whistoryDateFrom;
	private CCombo  whistoryDateTo;
	private FormData     fdlHistoryDate, fdHistoryDateFrom, fdHistoryDateTo;


	private Control addHistoryVarInputs(int middle, int margin, Control topControl) {
		// History Date params
		this.wlhistoryDates = new Label(sc, SWT.NONE);
		this.wlhistoryDates.setText("History date fields (from/to) :");
		props.setLook(this.wlhistoryDates);
		this.fdlHistoryDate = new FormData();
		this.fdlHistoryDate.left = new FormAttachment(0,0);
		this.fdlHistoryDate.top = new FormAttachment(topControl, margin);
		this.wlhistoryDates.setLayoutData(this.fdlHistoryDate);
		
		this.whistoryDateFrom = new CCombo(sc, SWT.NONE);
		props.setLook(this.whistoryDateFrom);
		this.fdHistoryDateFrom = new FormData();
		this.fdHistoryDateFrom.left = new FormAttachment(0,0);
		this.fdHistoryDateFrom.top = new FormAttachment(this.wlhistoryDates, margin*2);
		this.whistoryDateFrom.setLayoutData(this.fdHistoryDateFrom);
		this.addFieldToCombo(whistoryDateFrom);

		this.whistoryDateTo = new CCombo(sc, SWT.NONE);
		props.setLook(this.whistoryDateTo);
		this.fdHistoryDateTo = new FormData();
		this.fdHistoryDateTo.left = new FormAttachment(this.whistoryDateFrom,0);
		this.fdHistoryDateTo.top = new FormAttachment(this.wlhistoryDates, margin*2);
		this.whistoryDateTo.setLayoutData(this.fdHistoryDateTo);
		this.addFieldToCombo(whistoryDateTo);
		
		return whistoryDateTo;
	}
	
	private Label wlhistoryColumn;
	private Text  whistoryColumnFrom;
	private Text  whistoryColumnTo;
	private FormData     fdlHistoryColumn, fdHistoryColumnFrom, fdHistoryColumnTo;


	private Control addHistoryColumns(int middle, int margin, Control topControl) {
		// History Date params
		this.wlhistoryColumn = new Label(sc, SWT.NONE);
		this.wlhistoryColumn.setText("History columns (from/to):");
		props.setLook(this.wlhistoryColumn);
		this.fdlHistoryColumn = new FormData();
		this.fdlHistoryColumn.left = new FormAttachment(0,0);
		this.fdlHistoryColumn.top = new FormAttachment(topControl, margin);
		this.wlhistoryColumn.setLayoutData(this.fdlHistoryColumn);
		
		this.whistoryColumnFrom = new Text(sc, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(this.whistoryColumnFrom);
		this.fdHistoryColumnFrom = new FormData();
		this.fdHistoryColumnFrom.left = new FormAttachment(0,0);
		this.fdHistoryColumnFrom.top = new FormAttachment(this.wlhistoryColumn, margin*2);
		this.fdHistoryColumnFrom.width = 300; 
		this.whistoryColumnFrom.setLayoutData(this.fdHistoryColumnFrom);

		this.whistoryColumnTo = new Text(sc, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		props.setLook(this.whistoryColumnTo);
		this.fdHistoryColumnTo = new FormData();
		this.fdHistoryColumnTo.left = new FormAttachment(this.whistoryColumnFrom,0);
		this.fdHistoryColumnTo.top = new FormAttachment(this.wlhistoryColumn, margin*2);
		this.fdHistoryColumnTo.width = 300; 
		this.whistoryColumnTo.setLayoutData(this.fdHistoryColumnTo);
		
		return whistoryColumnTo;
	}

	
	
	public void setPosition(){
		
		String scr = wSQL.getText();
		int linenr = wSQL.getLineAtOffset(wSQL.getCaretOffset())+1;
		int posnr  = wSQL.getCaretOffset();
				
		// Go back from position to last CR: how many positions?
		int colnr=0;
		while (posnr>0 && scr.charAt(posnr-1)!='\n' && scr.charAt(posnr-1)!='\r')
		{
			posnr--;
			colnr++;
		}

		wlPosition.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.Position.Label",""+linenr, ""+colnr));

	}
	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	public void getData()
	{
		int i;
		logDebug(BaseMessages.getString(PKG, "DatabaseJoinDialog.Log.GettingKeyInfo")); //$NON-NLS-1$
		
		wSQL.setText( Const.NVL(input.getSql(), ""));
		String[] historyFields = input.getHistoryFields();
		if(historyFields != null) {
			if(historyFields[0] != null) this.whistoryDateFrom.setText(historyFields[0]);
			if(historyFields[1] != null) this.whistoryDateTo.setText(historyFields[1]);
		}
		String[] historyColumns = input.getHistoryColumns();
		if(historyColumns != null) {
			if(historyColumns[0] != null) this.whistoryColumnFrom.setText(historyColumns[0]); 
			if(historyColumns[1] != null) this.whistoryColumnTo.setText(historyColumns[1]); 
		}
		AddTypes addTypes = input.getAddType();
		if(addTypes == null)
			addTypes = AddTypes.Day;
		this.wAddType.setText(addTypes.name());
		wuseVars.setSelection(input.isVariableReplace());
		if (input.getParameterField()!=null)
		for (i=0;i<input.getParameterField().length;i++)
		{
			TableItem item = wParam.table.getItem(i);
			if (input.getParameterField()[i]  !=null) item.setText(1, input.getParameterField()[i]);
			if (input.getParameterType() [i]  !=0   ) item.setText(2, ValueMeta.getTypeDesc( input.getParameterType()[i] ));
		}
		
		if (input.getDatabaseMeta()!=null)   wConnection.setText(input.getDatabaseMeta().getName());
		else if (transMeta.nrDatabases()==1)
		{
			wConnection.setText( transMeta.getDatabase(0).getName() );
		}

		wStepname.selectAll();
		wParam.setRowNums();
		wParam.optWidth(true);
	}
	
	private void cancel()
	{
		stepname=null;
		input.setChanged(backupChanged);
		dispose();
	}
	
	private void ok()
	{
		if (Const.isEmpty(wStepname.getText())) return;

		int nrparam  = wParam.nrNonEmpty();
		
		input.allocate(nrparam);
		
		input.setSql( wSQL.getText() );
		AddTypes addType = AddTypes.Day;
		String wAddTypeText = this.wAddType.getText();
		if(wAddTypeText != null && wAddTypeText.length() > 0)
			addType = AddTypes.valueOf(wAddTypeText);
		input.setAddType(addType);
		
		input.setVariableReplace(wuseVars.getSelection() );
		input.setHistoryFields(new String[] {this.whistoryDateFrom.getText(), this.whistoryDateTo.getText()});
		input.setHistoryColumns(new String[] {this.whistoryColumnFrom.getText(), this.whistoryColumnTo.getText()});
		logDebug(BaseMessages.getString(PKG, "DatabaseJoinDialog.Log.ParametersFound")+nrparam+" parameters"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0;i<nrparam;i++)
		{
			TableItem item = wParam.getNonEmpty(i);
			input.getParameterField()[i]   = item.getText(1);
			input.getParameterType() [i]   = ValueMeta.getType( item.getText(2) );
		}

		input.setDatabaseMeta( transMeta.findDatabase(wConnection.getText()) );

		stepname = wStepname.getText(); // return value

		if (transMeta.findDatabase(wConnection.getText())==null)
		{
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(BaseMessages.getString(PKG, "DatabaseJoinDialog.InvalidConnection.DialogMessage")); //$NON-NLS-1$
			mb.setText(BaseMessages.getString(PKG, "DatabaseJoinDialog.InvalidConnection.DialogTitle")); //$NON-NLS-1$
			mb.open();
		}
		
		dispose();
	}

	private void get()
	{
		try
		{
			RowMetaInterface r = transMeta.getPrevStepFields(stepname);
			if (r!=null && !r.isEmpty())
			{
                BaseStepDialog.getFieldsFromPrevious(r, wParam, 1, new int[] { 1 }, new int[] { 2 }, -1, -1, null);
			}
		}
		catch(KettleException ke)
		{
			new ErrorDialog(shell, BaseMessages.getString(PKG, "DatabaseJoinDialog.GetFieldsFailed.DialogTitle"), BaseMessages.getString(PKG, "DatabaseJoinDialog.GetFieldsFailed.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
