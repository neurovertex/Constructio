package eu.neurovertex.constructio.gui;

import eu.neurovertex.constructio.Distances;
import eu.neurovertex.constructio.Grid;
import eu.neurovertex.constructio.Grid.Square;
import eu.neurovertex.constructio.GridUtils;
import eu.neurovertex.constructio.entities.AbstractFreeEntity;
import eu.neurovertex.constructio.entities.FreeEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Neurovertex
 *         Date: 01/10/2014, 16:19
 */
public class GridPanel extends JPanel {
	private Grid grid;
	private static final int side = 32;
	private static final Color COL_SOLID = Color.WHITE,
								COL_NONSOLID = new Color(128, 128, 255, 128);
	private Optional<Square> srcSquare = Optional.empty(), dstSquare = Optional.empty();
	private Optional<GridUtils.Path> path = Optional.empty();
	private int lastButton;

	public GridPanel(Grid gr) {
		this.grid = gr;
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(side*grid.getWidth(), side*grid.getHeight()));
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				lastButton = e.getButton();
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				Square sq = grid.get(Math.min(e.getX() / side, grid.getWidth() - 1), Math.min(e.getY() / side, grid.getHeight() - 1));
				if (dstSquare.isPresent() || !srcSquare.isPresent()) {
					srcSquare = Optional.of(sq);
					dstSquare = Optional.empty();
					updatePath();
				} else {
					dstSquare = Optional.of(sq);
					updatePath();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3)
					srcSquare = dstSquare = Optional.empty();
				if (path.isPresent())
					new AbstractFreeEntity(path.get().getFirst(), 0.1) {
						@Override
						public void update() {
							super.update();
							if (!getPath().isPresent())
								destroyEntity();
						}
					}.setPath(path.get());
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (lastButton != MouseEvent.BUTTON1)
					return;
				Square sq = grid.get(Math.min(Math.max(e.getX(), 0) / side, grid.getWidth() - 1), Math.min(Math.max(e.getY(), 0) / side, grid.getHeight() - 1));
				if (srcSquare.isPresent() && sq != srcSquare.get() && (!dstSquare.isPresent() || dstSquare.get() != sq)) {
					dstSquare = Optional.of(sq);
					updatePath();
				}
			}
		});
	}

	private void updatePath() {
		Optional<GridUtils.Path> current = path;
		if (srcSquare.isPresent() && dstSquare.isPresent())
			path = GridUtils.pathFinder().with(Distances.groundDistance()).from(srcSquare.get()).to(dstSquare.get()).find(grid);
		else
			path = Optional.empty();
		// If path changed
		if (current.isPresent() ^ path.isPresent() || current.map(c -> c.hashCode() != path.get().hashCode()).orElse(false))
			repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		final int w = getWidth(), h = getHeight();

		BufferedImage img = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graph = img.createGraphics();
		for (int i = 0; i < grid.getWidth(); i ++)
			for (int j = 0; j < grid.getHeight(); j ++) {
				graph.clearRect(0, 0, side, side);
				paintSquare(graph, grid.get(i, j));
				g.drawImage(img, i * side, j * side, null);
			}
		path.ifPresent(p -> paintPath(g, new Color(255, 64, 64, 192), p));

		grid.getFreeEntities().stream().forEach(e -> paintEntity(g, e));

		g.setColor(Color.GRAY);
		for (int i = 1; i < grid.getWidth(); i ++) {
			g.drawLine(i*side, 0, i*side, h-1);
		}
		for (int i = 1; i < grid.getHeight(); i ++) {
			g.drawLine(0, i*side, w-1, i*side);
		}
	}

	private void paintSquare(Graphics2D g, Square square) {
		if (!square.getEntity().isPresent())
			return;
		Grid.GridEntity entity = square.getEntity().get();
		if (entity.isSolid(square)) {
			g.setColor(COL_SOLID);
			g.fillRect(side / 8, side / 8, 6 * side / 8, 6 * side / 8);
		} else {
			g.setColor(COL_NONSOLID);
			g.drawOval(side/4, side/4, side/2, side/2);
		}
	}

	private void paintPath(Graphics g, Color nodeCol, GridUtils.Path path) {
		int rad = side/2, width = side/3, radOffset = (side-rad)/2, widthOffset = (side-width)/2;
		Collection<Square> squares = path.getPath();
		g.setColor(nodeCol);
		squares.stream().map(sq -> new Point(sq.getX()*side+radOffset, sq.getY()*side+radOffset))
						.forEach(p -> g.fillOval(p.x, p.y, rad, rad));
	}

	private void paintEntity(Graphics g, FreeEntity entity) {
		int size = side/2, offset = side/2-size/2;
		g.setColor(Color.GREEN);
		g.drawOval((int)Math.round(entity.getX()*side+offset), (int)Math.round(entity.getY()*side+offset), size, size);
	}
}
