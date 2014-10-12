package eu.neurovertex.constructio.gui;

import eu.neurovertex.constructio.*;
import eu.neurovertex.constructio.Grid.Square;
import eu.neurovertex.constructio.entities.AbstractFreeEntity;
import eu.neurovertex.constructio.entities.FreeEntity;
import eu.neurovertex.constructio.entities.ShitMover;
import eu.neurovertex.constructio.entities.Stockpile;

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
		setPreferredSize(new Dimension(side * grid.getWidth(), side * grid.getHeight()));
		setMaximumSize(new Dimension(side * grid.getWidth(), side * grid.getHeight()));
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
						public void onArrival() {
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
	protected void paintComponent(Graphics graph) {
		super.paintComponent(graph);
		final int w = getWidth(), h = getHeight();
		graph.setClip(0, 0, side*grid.getWidth(), side*grid.getHeight());
		{
			BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < grid.getWidth(); i++)
				for (int j = 0; j < grid.getHeight(); j++)
					paintSquare(img.getSubimage(i * side, j * side, side, side).createGraphics(), grid.get(i, j));
			graph.drawImage(img, 0, 0, null);
		}

		graph.setColor(Color.GRAY);
		for (int i = 1; i < grid.getWidth(); i ++) {
			graph.drawRect(i * side - 1, 0, 1, h);
		}
		for (int i = 1; i < grid.getHeight(); i ++) {
			graph.drawRect(0, i * side - 1, w, 1);
		}
		grid.getFreeEntities().stream().filter(entity -> entity instanceof AbstractFreeEntity && ((AbstractFreeEntity)entity).getPath().isPresent())
				.forEach(e -> paintPath(graph, new Color(255, 92, 92, 128), ((AbstractFreeEntity) e).getPath().get()));
		path.ifPresent(p -> paintPath(graph, new Color(255, 92, 92, 64), p));
		grid.getFreeEntities().stream().forEach(e -> paintEntity(graph, e));
	}

	private void paintSquare(Graphics2D g, Square square) {
		g.setColor(square.getTerrain().getColor());
		g.fillRect(0, 0, side, side);
		if (square.getEntity().isPresent()) {
			Grid.GridEntity entity = square.getEntity().get();
			if (entity.isSolid(square)) {
				g.setColor(COL_SOLID);
				g.fillRoundRect(0, 0, side, side, side / 8, side / 8);
				if (entity instanceof Stockpile) {
					Stockpile sp = (Stockpile) entity;
					Inventory inv = sp.getInventory(square.getX() - sp.getX(), square.getY() - sp.getY());
					int areaSide = side * 3 / 4, areaOffset = side / 8;
					g.clearRect(areaOffset, areaOffset, areaSide, areaSide);
					paintInventory(g, square, inv, areaSide, areaOffset );
				}
			} else {
				g.setColor(COL_NONSOLID);
				g.drawOval(side / 4, side / 4, side / 2, side / 2);
			}
		}
	}

	private void paintInventory(Graphics2D g, Square square, Inventory inv, int areaSide, int areaOffset) {
		if (inv.getAmount() > 0) {
			int amount = inv.getAmount()*areaSide / inv.getCapacity(), offset = areaSide - amount;
			Resource r = inv.getResources().iterator().next();
			g.setColor(r.getColor());
			g.fillRect(areaOffset, areaOffset+offset, areaSide, amount);
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
		int size = side*3/4, offset = (side-size)/2;
		g.setColor(Color.GREEN.darker());
		g.fillOval((int) Math.round(entity.getX() * side + offset), (int) Math.round(entity.getY() * side + offset), size, size);
		if (entity instanceof ShitMover && ((ShitMover)entity).getInventory().getAmount() > 0) {
			size = size*2/3;
			offset = (side-size)/2;
			Inventory inv = ((ShitMover)entity).getInventory();
			int angle = inv.getAmount()*360 / inv.getCapacity();
			g.setColor(inv.getResources().iterator().next().getColor());
			g.fillArc((int)Math.round(entity.getX()*side+offset), (int)Math.round(entity.getY()*side+offset), size, size, 0, angle);
		}
	}
}
