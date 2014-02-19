import org.orbisgis.sif.*
import org.orbisgis.sif.multiInputPanel.*

ds = mc.getDataManager().getDataSource()
sql = groovy.sql.Sql.newInstance(ds)
mip = new MultiInputPanel("Buffer GUI")

ln = mc.getLayers().collect {	it.getName() }
mip.addInput("1", "Input table", new ComboBoxChoice(ln as String[]))
mip.addInput("2", "New column name", new TextBoxType())
mip.addInput("3", "Buffer size", new TextBoxType())

if (UIFactory.showDialog(mip)) {
	
	inputTable = mip.getInput("1")
	outputTable = inputTable + "_buffer"
	newColName = mip.getInput("2")
	bufferSize = mip.getInput("3")

	if (!newColName.isEmpty()) {
		sql.execute "DROP TABLE IF EXISTS " + outputTable
		sql.execute "CREATE TABLE " + outputTable + " AS SELECT * FROM " + inputTable + " LIMIT 0"
		sql.execute "ALTER TABLE " + outputTable + " ADD COLUMN " + newColName + " POLYGON BEFORE the_geom"
		
		getColumnNames = { name ->
			sql.rows("SELECT * FROM " + name + " LIMIT 0") { row ->
				colNames = (1..row.columnCount).collect { row.getColumnName(it) }
			}
			colNames
		}
		
		buftable = sql.dataSet(outputTable)
		sql.rows("SELECT * FROM " + inputTable).each {
			map = [:]
			map[newColName] = it.the_geom.buffer(bufferSize.toDouble())
			getColumnNames(inputTable).each { name ->
				map[name] = it.get(name)
			}
			buftable.add(map)
		}
	} else {
		println "New column name must not be empty."
	}
} else {
    println "cancelled"
}
