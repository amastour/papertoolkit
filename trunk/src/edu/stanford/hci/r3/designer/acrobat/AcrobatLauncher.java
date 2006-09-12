package edu.stanford.hci.r3.designer.acrobat;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.*;

import edu.stanford.hci.r3.PaperToolkit;
import edu.stanford.hci.r3.util.WindowUtils;
import edu.stanford.hci.r3.util.components.BufferedImagePanel;
import edu.stanford.hci.r3.util.graphics.ImageCache;

/**
 * <p>
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>.</span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class AcrobatLauncher {

	private static BufferedImagePanel activeArea;

	private static DropTarget dropTarget;

	private static DropTargetAdapter dropTargetAdapter;

	private static FileTransferHandler fileTransferHandler;

	private static JFrame frame;

	private static JPanel mainPanel;

	private static BufferedImage pdfLogo;

	private static final Color TRANSLUCENT_GRAY = new Color(50, 50, 50, 88);

	private static final Font FONT = new Font("Trebuchet MS", Font.BOLD, 28);

	/**
	 * @return
	 */
	private static Component getActiveArea() {
		if (activeArea == null) {
			activeArea = new BufferedImagePanel() {
				public void paintComponent(Graphics g) {
					super.paintComponent(g);
					Graphics2D g2d = (Graphics2D) g;
					// draw list of file names

					if (onlyPDFs != null) {
						g.setColor(TRANSLUCENT_GRAY);
						g.setFont(FONT);
						int stringY = (int) ((activeArea.getHeight() - onlyPDFs.size() * 40) / 2.0);
						for (File f : onlyPDFs) {
							g.drawString(f.getName(), 50, stringY);
							stringY += 50;
						}
					}
				}
			};
			activeArea.setBorder(BorderFactory.createMatteBorder(10, 10, 10, 10, Color.LIGHT_GRAY));
			activeArea.setBackground(Color.DARK_GRAY);
			fileTransferHandler = new FileTransferHandler();
			activeArea.setTransferHandler(fileTransferHandler);

			try {
				dropTarget = new DropTarget();
				dropTarget.setComponent(activeArea);
				dropTarget.addDropTargetListener(getDropTargetAdapter());
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}

			pdfLogo = ImageCache.loadBufferedImage(new File("data/icons/pdfIcon.png"));
		}
		return activeArea;
	}

	private static List<File> onlyPDFs;

	/**
	 * @return
	 */
	private static DropTargetAdapter getDropTargetAdapter() {
		if (dropTargetAdapter == null) {
			dropTargetAdapter = new DropTargetAdapter() {
				public void dragEnter(DropTargetDragEvent dtde) {
					frame.setAlwaysOnTop(true);
					frame.setAlwaysOnTop(false);

					// System.out.println(FileTransferHandler.hasFileFlavor(dtde.getCurrentDataFlavors()));
					// System.out.println(activeArea.getTransferHandler());
					// if there are PDFs in the list...
					onlyPDFs = fileTransferHandler.getOnlyPDFs(activeArea, dtde.getTransferable());
					// show the icon
					if (onlyPDFs.size() > 0) {
						activeArea.setBackground(Color.WHITE);
						activeArea.setImageCentered(pdfLogo);
					}
				}

				/**
				 * @see java.awt.dnd.DropTargetAdapter#dragExit(java.awt.dnd.DropTargetEvent)
				 */
				public void dragExit(DropTargetEvent dte) {
					// hide background
					hideBackground();
				}

				/**
				 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
				 */
				public void drop(DropTargetDropEvent dtde) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					fileTransferHandler.importData(activeArea, dtde.getTransferable());

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// hide background
					hideBackground();
				}

				private void hideBackground() {
					activeArea.setBackground(Color.DARK_GRAY);
					activeArea.setImage(null);
					onlyPDFs = null;
				}
			};
		}
		return dropTargetAdapter;
	}

	/**
	 * @return
	 */
	private static Component getLabel() {
		JLabel label = new JLabel(
				"Drag a PDF on to the active area below to start the R3 Acrobat Designer.");
		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return label;
	}

	/**
	 * @return
	 */
	private static Container getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(getLabel(), BorderLayout.NORTH);
			mainPanel.add(getActiveArea(), BorderLayout.CENTER);
		}
		return mainPanel;
	}

	/**
	 * Sep 12, 2006
	 */
	public static void main(String[] args) {
		PaperToolkit.initializeLookAndFeel();
		frame = new JFrame("Acrobat Designer Launcher");
		frame.setContentPane(getMainPanel());
		frame.setSize(640, 480);
		frame.setLocation(WindowUtils.getWindowOrigin(frame, WindowUtils.DESKTOP_CENTER));
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}