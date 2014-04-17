di-history-join-plugin
======================

Plugin for Pentaho Data Integration used to supply a method to join two tables 
using the date-from and	date-to history. 
It use the two dates that indicate the life of the record and join using a 
query (like the database join plugin) to resolve the record's story of the two entities. 


Installation
======================

To install the plugin download the favorite archive from https://sourceforge.net/projects/dihistoryjoinplugin/files/ and extract it in $PENTAHO_DI_HOME/plugins/steps and joyit!

Usage
======================
To use the plugin open the <Joins> category and use the <History Join> plugin. Open the plugin, you can see all fields:
* Step name
* Connection : The database connection to use
* History date fields (from / to) : The fields that contains the two date (date from and date to) that
	drive the story.
* History columns (from/to) : The two column that contains the dates used to recreate the story
	of the principal entity.
* SQL : The query to use for the join. Please remember of add the join condition and the order for the date-from.
* Replace variables : If flagged the variables will replaced in the SQL.
* Parameters to use : Parameters of the query.


