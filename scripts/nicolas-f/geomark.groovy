import org.orbisgis.core.Services
import org.orbisgis.viewapi.docking.*
import javax.swing.*
import org.orbisgis.viewapi.edition.*
import groovy.swing.SwingBuilder
import org.orbisgis.view.icons.OrbisGISIcon
import org.orbisgis.mapeditorapi.MapElement;
import org.orbisgis.core.layerModel.MapContext;
import javax.swing.JList
import org.apache.log4j.Logger;
import com.vividsolutions.jts.geom.Envelope;

/**
 * A panel to manage map positionning.
 *
 * @author Nicolas FORTIN
 */
class MyPanel implements EditorDockable {

    /**Logger is a class to log messages to the user */
    Logger LOGGER = Logger.getLogger("gui."+MyPanel.class);
    /** Holds properties of the GUI panel like the title */
    DockingPanelParameters dockingParameters = new DockingPanelParameters()
    /** Panel SWING component */
    JPanel panel
    /** A Groovy class that helps build SWING components */
    SwingBuilder swing = new SwingBuilder()
    /** The current loaded map in OrbisGIS */
    MapContext mapContext
    /** SWING component, List of GeoMarks */
    JList geoMarkList    
    /** Internal array of GeoMark names */
    List listData = []
    /** Array of geomark envelopes (minX,maxX,minY,maxY) */
    Map envList = new HashMap() 

    /** Constructor*/
    MyPanel() {
        EditorManager editorManager = Services.getService(EditorManager.class)
            mapContext = MapElement.fetchFirstMapElement(editorManager).getMapContext();
        dockingParameters.setName("geomark")
            dockingParameters.setTitle("Geomark")
            dockingParameters.setDockActions(
                    [swing.action(closure: { remove() }, smallIcon: OrbisGISIcon.getIcon("remove"), name: "Remove panel"),
                    swing.action(closure: { addGeoMark() }, smallIcon: OrbisGISIcon.getIcon("add"), name: "Add GeoMark"),
                    swing.action(closure: { goToGeoMark() }, smallIcon: OrbisGISIcon.getIcon("zoom"), name: "Zoom to GeoMark"),
                    swing.action(closure: { deleteGeoMark() }, smallIcon: OrbisGISIcon.getIcon("delete"), name: "Delete GeoMark")
                    ])
            panel = swing.panel() {
                borderLayout()
                    scrollPane( constraints:CENTER ) {
                        geoMarkList = list(id:'listId')
                    }
            }
    }

    /** The user clicks on Zoom to GeoMark */
    void goToGeoMark() {
        // Read selected entry in the SWING component
        String key = geoMarkList.getSelectedValue()
            if(key!=null && !key.isEmpty()) {
                // Change bounding box, extract Envelope instance from the array this.envList
                mapContext.setBoundingBox(envList.get(key))
            }
    }

    /** The user clicks on Delete GeoMark*/
    void deleteGeoMark() {
        listData.remove(geoMarkList.getSelectedValue())
            envList.remove(geoMarkList.getSelectedValue())
            geoMarkList.listData = listData
    }

    /** The user clicks on Add GeoMark*/
    void addGeoMark() {
        try {
            Envelope env = mapContext.getBoundingBox()
                if (env != null) {
                    def label = env.toString()
                    def pane = swing.optionPane(message : "Set the GeoMark name", wantsInput: true)
                    pane.createDialog(panel, "Choose GeoMark name").show()
                    if (!pane.inputValue.isEmpty()) { label = pane.inputValue }
                    listData.add(label)
                    geoMarkList.listData = listData
                    envList.put(label, env)
                }
        } catch(Exception ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex)
        }
    }

    /**The user clicks on Remove panel*/
    void remove() {
        // Remove the panel from OrbisGIS
        EditorManager editorManager = Services.getService(EditorManager.class)
            editorManager.removeEditor(this)
    }

    @Override
        DockingPanelParameters getDockingParameters() {
            return dockingParameters
        }

    @Override
        JComponent getComponent() {
            return panel
        }

    /** This panel handles MapElement */
    @Override
        boolean match(org.orbisgis.viewapi.edition.EditableElement editableElement) {
            return editableElement instanceof MapElement
        }

    /** We do not store map elements */
    @Override
        EditableElement getEditableElement() {
            return null
        }

    /** The user wants to load another Map */
    @Override
        void setEditableElement(EditableElement editableElement) {
            if(editableElement instanceof MapElement) {
                mapContext = editableElement.getMapContext()
            }
        }
}

// MAIN STUFF
// Get the Panel manager
EditorManager editorManager = Services.getService(EditorManager.class)
// Construct our panel
EditorDockable panel = new MyPanel()
// Tell the panel manager to load our panel
editorManager.addEditor(panel)
