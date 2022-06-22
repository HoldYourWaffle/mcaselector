package net.querz.mcaselector.ui.dialog;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.querz.mcaselector.io.job.BiomeSelector;
import net.querz.mcaselector.text.Translation;
import net.querz.mcaselector.tile.TileMap;
import net.querz.mcaselector.ui.UIFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeSelectorDialog extends Dialog<BiomeSelectorDialog.Result> {

	private static final Logger LOGGER = LogManager.getLogger(BiomeSelectorDialog.class);

//	private final TextField filterQuery = new TextField();
	private final Label selectionRadiusLabel = UIFactory.label(Translation.DIALOG_FILTER_CHUNKS_SELECTION_RADIUS);
	private final TextField selectionRadius = new TextField();

	private BiomeSelector biomeSelector = new BiomeSelector();

	public BiomeSelectorDialog(Stage primaryStage, TileMap tileMap) {
		//TODO this shouldn't be a dialog but an input field in the menubar or statusbar or floating in the corner (toggle the tool in menu)

		titleProperty().bind(Translation.DIALOG_BIOME_SELECTOR_TITLE.getProperty());

		initModality(Modality.NONE);
		initStyle(StageStyle.UTILITY);

		getDialogPane().getStyleClass().add("biome-selector-pane");

		// apply same stylesheets to this dialog
		getDialogPane().getStylesheets().addAll(primaryStage.getScene().getStylesheets());

		getDialogPane().getButtonTypes().addAll(ButtonType.FINISH);
		getDialogPane().lookupButton(ButtonType.FINISH).addEventFilter(ActionEvent.ACTION, e -> {
			tileMap.getWindow().untrackDialog(this);
			tileMap.setBiomeSelector(null);
		});
		setOnCloseRequest(e -> {
			//if (!closedWithOK.get()) { CHECK necessary?
				tileMap.getWindow().untrackDialog(this);
			tileMap.setBiomeSelector(null);
			//}
		});

		setResizable(true);
		setX(primaryStage.getX());
		setY(primaryStage.getY());

		/*groupFilterBox.setOnUpdate(f -> {
			getDialogPane().lookupButton(ButtonType.OK).setDisable(!value.isValid());
			if (value.isValid()) {
				filterQuery.setText(value.toString());
			}

			if (value.selectionOnly()) {
				if (delete.isSelected()) {
					select.fire();
				}
				delete.setDisable(true);
			} else {
				delete.setDisable(false);
			}
		});

		filterQuery.setText(gf.toString());

		filterQuery.setOnAction(e -> {
			FilterParser fp = new FilterParser(filterQuery.getText());
			try {
				gf = fp.parse();
				gf = FilterParser.unwrap(gf);
				LOGGER.debug("parsed filter query from: {}, to: {}", filterQuery.getText(), gf);
				value = gf;
				groupFilterBox.setFilter(gf);
			} catch (ParseException ex) {
				LOGGER.warn("failed to parse filter query from: {}, error: {}", filterQuery.getText(), ex.getMessage());
			}
		});*/

		selectionRadius.setText(biomeSelector.getRadius() == 0 ? "" : ("" + biomeSelector.getRadius()));
		selectionRadius.textProperty().addListener((a, o, n) -> onSelectionRadiusInput(o, n));

		/*VBox actionBox = new VBox();
		actionBox.getChildren().addAll(select, export, delete);*/

		GridPane optionBox = new GridPane();
		optionBox.getStyleClass().add("biome-selector-dialog-option-box");
		optionBox.add(selectionRadiusLabel, 0, 2, 1, 1);
		optionBox.add(withStackPane(selectionRadius), 1, 2, 1, 1);

		HBox selectionBox = new HBox();
		selectionBox.getChildren().addAll(/*actionBox,*/ optionBox);

		VBox box = new VBox();
		box.getChildren().addAll(/*filterQuery, new Separator(),*/ selectionBox);
		getDialogPane().setContent(box);

		tileMap.getWindow().trackDialog(this);
		tileMap.setBiomeSelector(biomeSelector);
	}

	private StackPane withStackPane(Node n) {
		//CHECK move to util?
		StackPane stack = new StackPane();
		stack.getStyleClass().add("biome-selector-dialog-stack-pane");
		stack.getChildren().add(n);
		StackPane.setAlignment(n, Pos.CENTER);
		return stack;
	}

	private void onSelectionRadiusInput(String oldValue, String newValue) {
		//CHECK move to util?
		if (newValue.isEmpty()) {
			biomeSelector.setRadius(0);
		} else {
			if (!newValue.matches("[0-9]+")) {
				selectionRadius.setText(oldValue);
				return;
			}
			biomeSelector.setRadius(Integer.parseInt(newValue));
		}
	}

	public static class Result { }
}
