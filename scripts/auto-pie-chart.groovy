import groovy.beans.Bindable
import groovy.swing.SwingBuilder
import javax.swing.JOptionPane
import java.awt.Dimension;
import javax.swing.JFrame;
import org.h2gis.utilities.TableLocation
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.DefaultPieDataset;

/* Get an SQL object */
ds = mc.getDataManager().getDataSource()
sql = groovy.sql.Sql.newInstance(ds)

/* Construct dialog to get user input */
def sb = new SwingBuilder()
dialog = sb.panel() {
    gridLayout cols: 2, rows: 0
    label "Input table"
    c1 = comboBox id:"1", items:mc.getLayers().collect { it.getTableReference() }
    label "Column"
    textField id:"2"
    label text:"Column names: "
    label text:bind{
        sql.rows("select * from " + c1.selectedItem + " limit 0") { row ->
            colNames = (1..row.columnCount).collect { row.getColumnName(it) }
        }
        colNames
    }
}
answer = JOptionPane.showConfirmDialog(null, dialog,
    "User input",
    JOptionPane.OK_CANCEL_OPTION,
    JOptionPane.PLAIN_MESSAGE)

if (answer == JOptionPane.OK_OPTION) {
 
    /* Recover user input */
    table = TableLocation.parse(sb."1".selectedItem).getTable()
    column = sb."2".text

    /* Construct and show chart */
    dataset = new DefaultPieDataset()
    sql.eachRow("select " + column + " as unique_name, count(" + column + ") as number from " + table + " group by " + column + "") {
        dataset.setValue(it.unique_name, it.number)
    }
    chart = ChartFactory.createPieChart("Unique items", dataset);
    chartPanel = new ChartPanel(chart);
    chartPanel.setPreferredSize(new Dimension(500, 400));
    frame = new JFrame();
    frame.add(chartPanel);
    frame.pack();
    frame.setVisible(true);
} else {
    println "Canceled."
}
