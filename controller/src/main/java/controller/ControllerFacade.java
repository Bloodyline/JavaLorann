package controller;

import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.ExiaEngine.BoardFrame;
import org.ExiaEngine.BoardPanel;

import MotionElement.Enemies;
import MotionElement.Obstacle;
import MotionElement.Pawn;
import MotionElement.Player;
import MotionElement.Status;
import model.Example;
import model.IModel;
import view.IView;


/**
 * <h1>The Class ControllerFacade provides a facade of the Controller
 * component.</h1>
 *
 * @author Jean-Aymeric DIET jadiet@cesi.fr
 * @version 1.0
 */
public class ControllerFacade implements IController {

	/** The view. */
	private  IView view;

	/** The model. */
	private IModel model;

	private Thread gameState = new Thread() {
		@Override
		public void run() {
			checkPlayerState();
		}
	};

	

	/**
	 * Instantiates a new controller facade.
	 *
	 * @param view
	 *            the view
	 * @param model
	 *            the model
	 */
	public ControllerFacade(final IView view, final IModel model) {
		super();
		this.model = model;
		this.view = view;
		this.gameState.start();

	}

	/**
	 * Start.
	 *
	 * @throws SQLException
	 *             the SQL exception
	 * @throws InterruptedException
	 */
	public void start() throws SQLException, InterruptedException {
		this.model.setBoardframe(new BoardFrame("Lorann"));
		this.level();

	}

	public void level() throws InterruptedException {
		this.chooseLevel();
		this.drawMap(this.getModel().getLvl());
	}

	/**
	 * Function that ask the user a level to load w/ a option pan.
	 */
	public void chooseLevel() {
		DefaultTableModel tableModel = new DefaultTableModel();
		tableModel.addColumn("Levels:", new Object[] { "1", "2", "3", "4", "5" });

		JTable table = new JTable(tableModel);
		ListSelectionModel selectionModel = table.getSelectionModel();

		JPanel p = new JPanel(new BorderLayout());
		p.add(table, BorderLayout.CENTER);

		int option = JOptionPane.showConfirmDialog(null, p, "Choose a level:", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.INFORMATION_MESSAGE);

		if (JOptionPane.OK_OPTION == option) {
			this.getModel().setLvl(checkSelection(selectionModel, tableModel));
		} else {
			selectionModel.clearSelection();
		}

	}

	/**
	 * Function to get the selected value from the option pan.
	 * 
	 * @param selectionModel
	 * @param tableModel
	 * @return
	 */
	private static int checkSelection(ListSelectionModel selectionModel, TableModel tableModel) {
		for (int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
			if (selectionModel.isSelectedIndex(i)) {
				Object selectedValue = tableModel.getValueAt(i, 0);
				return Integer.parseInt((String) selectedValue);
			}
		}
		return 1;
	}

	/**
	 * Instantiate each object of the DB tile per tile like a bursh into the map. 
	 * @param lvl
	 * @throws InterruptedException
	 */
	public void drawMap(int lvl) throws InterruptedException {
		System.out.println("lvl: "+ lvl);
		lvl = lvl - 1;
		int start = (20 * 12) * lvl;
		int end = start + (20 * 12);

		// Load all the objects of the database into the model
		try {
			for (Example i : this.getModel().getMapByLvl(start, end)) {
				this.getModel().getMap().add(i.getName());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Load the objects into the game
		int x = 0;
		int y = 0;

		for (String i : this.getModel().getMap()) {

			if (x == 32 * 20) {
				x = 0;
				y += 32;
			}
			if (i.equalsIgnoreCase(this.getModel().VERTICALBONE)) {
				this.instanciateObject(x, y, true);
				x += 32;
			}
			if (i.equals(this.getModel().HORIZONTALBONE)) {

				this.instanciateObject(x, y, false);
				x += 32;
			}
			if (i.equals(this.getModel().ROUNDOBSTACLE)) {
				this.instanciateObject(x, y, Status.OBSTACLE);
				x += 32;
			}
			if (i.equals(this.getModel().PLAYER)) {
				this.instanciateObject(x, y, this.model.getBoardframe());
				x += 32;
			}
			if (i.equals(this.getModel().MONSTER)) {
				this.instanciateObject(x, y, IModel.FOV);
				x += 32;
			}
			if (i.equals(this.getModel().PURSE)) {
				this.instanciateObject(x, y, Status.PURSE);
				x += 32;
			}
			if (i.equals(this.getModel().CRYSTAL)) {
				this.instanciateObject(x, y, Status.CRYSTAL);
				x += 32;
			}
			if (i.equals(this.getModel().GATE)) {
				this.instanciateObject(x, y, Status.GATE_CLOSED);
				x += 32;
			}
			if (i.equals(this.getModel().VOID)) {
				x += 32;
			}
		}

	}

	public void resetMap() {
		Pawn.resetPawns();
		for (int x = 0; x < this.getModel().getBoardframe().getPan().getTile().getWidth(); x++) {
			for (int y = 0; y < this.getModel().getBoardframe().getPan().getTile().getHeight(); y++) {
				BoardPanel.removeObject(x * 32, y * 32);
			}
		}
		this.getModel().setMap(new ArrayList<String>());
		this.getModel().getPlayer().kill(false);
	}

	public void checkPlayerState() {
		while (true) {
			try {
				System.out.println(Pawn.getPawns().size());
				if (!this.getModel().getPlayer().isAlive()) {
					this.resetMap();
					JOptionPane.showMessageDialog(new JFrame("Error"),
							"Game Over !\nYour Score : " + this.getModel().getPlayer().getScore());
					this.getModel().getBoardframe().dispose();

					try {
						this.start();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (Exception e) {

			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Instantiate a vertical or horizontal bone
	 * 
	 * @param x
	 * @param y
	 * @param isVertical
	 */
	public void instanciateObject(int x, int y, boolean isVertical) {
		BoardPanel.addObject(new Obstacle(x, y, isVertical), x, y);
	}

	/**
	 * Instantiate a purse or a gate or a crystal
	 * 
	 * @param x
	 * @param y
	 * @param status
	 */
	public void instanciateObject(int x, int y, Status status) {
		BoardPanel.addObject(new Obstacle(x, y, status), x, y);
	}

	/**
	 * Instantiate the player
	 * 
	 * @param x
	 * @param y
	 * @param boardframe
	 * @throws InterruptedException
	 */
	public void instanciateObject(int x, int y, BoardFrame boardframe) throws InterruptedException {
		this.model.setPlayer(new Player(x, y, boardframe));

	}

	/**
	 * Instantiate the enemy
	 * 
	 * @param x
	 * @param y
	 * @param FOV
	 */
	public void instanciateObject(int x, int y, int FOV) {
		try {
			new Enemies(x, y, FOV);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the view.
	 *
	 * @return the view
	 */
	public IView getView() {
		return this.view;
	}

	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	public IModel getModel() {
		return this.model;
	}
}
