package org.noroomattheinn.visibletesla;

import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import jfxtras.labs.scene.control.CalendarPicker;


public class DateRangeDialog implements DialogUtils.DialogController {

    private Stage stage;
    private Calendar start = null;
    private Calendar end = null;
    private boolean selectedAll = false;
    
    @FXML private AnchorPane root;
    @FXML private ResourceBundle resources;

    @FXML private URL location;

    @FXML private Button allButton;
    @FXML private Button cancelButton;
    @FXML private Button selectedButton;

    @FXML private CalendarPicker calendarPicker;

    @FXML private ComboBox<String> quickSelect;

    @FXML void buttonHandler(ActionEvent event) {
        Button b = (Button)event.getSource();
        if (b == allButton) {
            selectedAll = true;
        } else if (b == selectedButton) {
            List<Calendar> selection = calendarPicker.calendars();
            if (selection == null || selection.isEmpty()) {
                start = end = null;
            } else {
                start = selection.get(0);
                end = selection.get(selection.size()-1);
            }
        } else if (b == cancelButton) {
            start = end = null;
        }
        stage.close();
    }

    public boolean selectedAll() { return selectedAll; }
    
    public Calendar getStartCalendar() {
        if (start == null) return null;
        return decodeDay(encodeDay(start), false);
    }
    
    public Calendar getEndCalendar() {
        if (end == null) return null;
        return decodeDay(encodeDay(end), true);
    }
    
    @FXML void initialize() {
        quickSelect.valueProperty().addListener(handleQuickSelect);
    }

    @Override public void setStage(Stage stage) { this.stage = stage; }

    private final ChangeListener<String> handleQuickSelect = new ChangeListener<String>() {
        @Override public void changed(
                ObservableValue<? extends String> ov, String t, String t1) {
            Calendar now = Calendar.getInstance();
            DataStore.LoadPeriod period = DataStore.nameToLoadPeriod.get(t1);
            start = null;
            end = null;

            switch (period) {
                case Last7:
                    end = now;
                    start = Calendar.getInstance();
                    start.add(Calendar.DAY_OF_YEAR, -6);
                    break;
                case Last14:
                    end = now;
                    start = Calendar.getInstance();
                    start.add(Calendar.DAY_OF_YEAR, -13);
                    break;
                case Last30:
                    end = now;
                    start = Calendar.getInstance();
                    start.add(Calendar.DAY_OF_YEAR, -29);
                    break;                        
                case ThisWeek:
                    end = now;
                    start = Calendar.getInstance();
                    start.set(Calendar.DAY_OF_WEEK,
                            now.getActualMinimum(Calendar.DAY_OF_WEEK));
                    break;                        
                case ThisMonth:
                    end = now;
                    start = Calendar.getInstance();
                    start.set(Calendar.DAY_OF_MONTH,
                            now.getActualMinimum(Calendar.DAY_OF_MONTH));
                    break;
                case None:
                default: break;
            }
            
            ObservableList<Calendar> calendars = calendarPicker.calendars();
            calendars.clear();
            if (start != null) {
                int startDay = encodeDay(start);
                int endDay = encodeDay(end);
                int curDay = startDay;
                while (curDay <= endDay) {
                    Calendar c = decodeDay(curDay, curDay == endDay);
                    calendars.add(c);
                    Calendar next = (Calendar)c.clone();
                    next.add(Calendar.DAY_OF_YEAR, 1);
                    curDay = encodeDay(next);
                }
            }
        }
    };
    
    private int encodeDay(Calendar day) {
        int encoded = day.get(Calendar.YEAR);
        encoded = (encoded * 1000) + day.get(Calendar.DAY_OF_YEAR);
        return encoded;
    }
    
    private Calendar decodeDay(int encoded, boolean endOfDay) {
        int year = encoded / 1000;
        int dayOfYear = encoded % 1000;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_YEAR, dayOfYear);
        c.set(Calendar.HOUR_OF_DAY, endOfDay ? 23 : 0);
        c.set(Calendar.MINUTE, endOfDay ? 59 : 0);
        c.set(Calendar.SECOND, endOfDay ? 59 : 0);
        return c;
    }
}
