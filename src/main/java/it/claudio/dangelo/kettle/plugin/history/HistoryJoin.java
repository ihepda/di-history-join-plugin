package it.claudio.dangelo.kettle.plugin.history;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class HistoryJoin extends BaseStep implements StepInterface
{
	private static Class<?> PKG = HistoryJoinMeta.class; 

	private HistoryJoinMeta meta;
	private HistoryJoinData data;
	
	public HistoryJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	
	private synchronized void lookupValues(RowMetaInterface rowMeta, Object[] rowData) throws KettleException {
		if(first) {
			// if is the first time set the parameters row meta and add to the output row meta new fields 
			first = false;
			data.outputRowMeta = rowMeta.clone();
			//add fields of the step to the actual row meta
			meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { meta.getTableFields(), }, null, this);
			// parameters row meta?
			data.lookupRowMeta = new RowMeta();
			// contains the index of the fields used like parameters in the row meta
			data.keynrs = new int[meta.getParameterField().length];
			for (int i=0;i<meta.getParameterField().length;i++) {
				data.keynrs[i]=rowMeta.indexOfValue(meta.getParameterField()[i]);
				if (data.keynrs[i]<0) 
					throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseJoin.Exception.FieldNotFound",meta.getParameterField()[i])); 
				
				data.lookupRowMeta.addValueMeta( rowMeta.getValueMeta(data.keynrs[i]).clone() );
			}
			// resolve the history dates fields index
			data.historyFieldFrom = rowMeta.indexOfValue(meta.getHistoryFields()[0]);
			if(data.historyFieldFrom < 0)
				throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseJoin.Exception.FieldNotFound",meta.getHistoryFields()[0])); 
			data.historyFieldTo = rowMeta.indexOfValue(meta.getHistoryFields()[1]);
			if(data.historyFieldTo < 0)
				throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseJoin.Exception.FieldNotFound",meta.getHistoryFields()[1])); 
		}
		Date historyFieldFrom = (Date) rowData[data.historyFieldFrom];
		Date historyFieldTo = (Date) rowData[data.historyFieldTo];
		// Construct the parameters row values...
		Object[] lookupRowData = new Object[data.lookupRowMeta.size()];
		for (int i=0;i<data.keynrs.length;i++)
		{
			lookupRowData[i] = rowData[ data.keynrs[i] ];
		}
		
		// Set the values on the prepared statement (for faster exec.)
		ResultSet rs = data.db.openQuery(data.pstmt, data.lookupRowMeta, lookupRowData);
		// Get a row from the database...
		//
		Object[] add = data.db.getRow(rs);
		RowMetaInterface addMeta = data.db.getReturnRowMeta();
		/*
	     * Increments the number of lines read from an input source: database, file, socket, etc.
	     * @return the new incremented value
	     */
		incrementLinesInput();
		if(add == null) 
			addNullToRow(rowMeta, rowData);
			/*
		     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
		     * (synchronized) If distribute is true, a row is copied only once to the output rowsets, otherwise copies are sent
		     * to each rowset!
		     *
		     * @param row The row to put to the destination rowset(s).
		     * @throws KettleStepException
		     */
//			putRow(data.outputRowMeta, newRow);
			
		else {
			boolean lastWrite = false;
			while(true) {
				if(data.historyColumnFrom < 0) {
					data.historyColumnFrom = addMeta.indexOfValue(meta.getHistoryColumns()[0]);
					if(data.historyColumnFrom < 0)
						throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseJoin.Exception.FieldNotFound",meta.getHistoryColumns()[0])); 
				}
				if(data.historyColumnTo < 0) {
					data.historyColumnTo = addMeta.indexOfValue(meta.getHistoryColumns()[1]);
					if(data.historyColumnTo < 0)
						throw new KettleStepException(BaseMessages.getString(PKG, "DatabaseJoin.Exception.FieldNotFound",meta.getHistoryColumns()[1])); 
				}
				Date historyColumnFrom = (Date) add[data.historyColumnFrom];
				Date historyColumnTo = (Date) add[data.historyColumnTo];
				PeriodUtil instance = PeriodUtil.getInstance(historyFieldFrom, historyFieldTo, historyColumnFrom, historyColumnTo, this.meta.getAddType());
				if(instance == null) {
					lastWrite = false;
					Object [] nextAdd = data.db.getRow(rs);
					if(nextAdd == null) break;
					add = nextAdd;
					continue;
				}
				for (int i = 1; i < instance.getNumberOfRows(); i++) {
					instance.next();
					rowData[data.historyFieldFrom] = instance.getPeriod()[0];
					rowData[data.historyFieldTo] = instance.getPeriod()[1];
					if(instance.writeNewRow())
						this.addDataToRow(rowMeta, rowData, add, addMeta);
					else
						this.addNullToRow(rowMeta, rowData);
				}
				instance.next();
				historyFieldFrom = instance.getPeriod()[0];
				lastWrite = instance.writeNewRow();
				Object [] nextAdd = data.db.getRow(rs);
				if(nextAdd == null) break;
				add = nextAdd;
			}
			rowData[data.historyFieldFrom] = historyFieldFrom;
			rowData[data.historyFieldTo] = historyFieldTo;
			if(lastWrite)
				this.addDataToRow(rowMeta, rowData, add, addMeta);
			else
				this.addNullToRow(rowMeta, rowData);
				
		}
		data.db.closeQuery(rs);

		
	}

	private Object[] addDataToRow(RowMetaInterface rowMeta, Object[] rowData,
			Object[] dbRow, RowMetaInterface dbRowMeta) throws KettleStepException {
		Object[] newRow = Arrays.copyOf(rowData, rowData.length); 
		newRow = RowDataUtil.resizeArray(newRow, data.outputRowMeta.size());
		int newIndex = rowMeta.size();
		for (int i=0;i<dbRowMeta.size();i++) {
			newRow[newIndex++] = dbRow[i];
		}
		putRow(data.outputRowMeta, newRow);
		incrementLinesOutput();
		return newRow;
	}

	private Object[] addNullToRow(RowMetaInterface rowMetaResult,
			Object[] rowData) throws KettleStepException {
//		if (data.notfound==null) {
//			// Just return null values for all values...
//			// and save array for next use
//			data.notfound = new Object[data.db.getReturnRowMeta().size()];
//		}
		// create a new array of datas with the new metadata
		Object[] newRow = Arrays.copyOf(rowData, rowData.length); 
//		newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
		newRow = RowDataUtil.resizeArray(newRow, data.outputRowMeta.size());
		int newIndex = rowMetaResult.size();
		for (int i=0;i<data.db.getReturnRowMeta().size();i++) {
//		for (int i=0;i<data.notfound.length;i++) {
//			newRow[newIndex++] = data.notfound[i];
			newRow[newIndex++] = null;
		}
		putRow(data.outputRowMeta, newRow);
		incrementLinesOutput();
		return newRow;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException{
		meta=(HistoryJoinMeta)smi;
		data=(HistoryJoinData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		  
		try
		{
			lookupValues(getInputRowMeta(), r); // add new values to the row in rowset[0].
			
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "DatabaseJoin.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
		}
		catch(KettleException e)
		{

			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
			else
			{

				logError(BaseMessages.getString(PKG, "DatabaseJoin.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "DBJOIN001");
			}
		}		
			
		return true;
	}
    
    /** Stop the running query */
	@Override
    public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(HistoryJoinMeta)smi;
        data=(HistoryJoinData)sdi;

        if (data.db!=null && !data.isCanceled)
        {
          synchronized(data.db) {
            data.db.cancelStatement(data.pstmt);
          }
          setStopped(true);
          data.isCanceled=true;
        }
    }

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(HistoryJoinMeta)smi;
		data=(HistoryJoinData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			
			try
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
				
                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "DatabaseJoin.Log.ConnectedToDB")); //$NON-NLS-1$
	
                String sql=meta.getSql();
                if(meta.isVariableReplace()) sql=environmentSubstitute(sql);
				// Prepare the SQL statement
				data.pstmt = data.db.prepareSQL(sql);
				if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "DatabaseJoin.Log.SQLStatement",sql));
//				data.db.setQueryLimit(meta.getRowLimit());
				
				return true;
			}
			catch(KettleException e)
			{
				logError(BaseMessages.getString(PKG, "DatabaseJoin.Log.DatabaseError")+e.getMessage()); //$NON-NLS-1$
				if (data.db!=null) {
                	data.db.disconnect();
				}
			}
		}
		
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (HistoryJoinMeta)smi;
	    data = (HistoryJoinData)sdi;
	    
	    if (data.db!=null) {
        	data.db.disconnect();
	    }
	    
	    super.dispose(smi, sdi);
	}	
}
