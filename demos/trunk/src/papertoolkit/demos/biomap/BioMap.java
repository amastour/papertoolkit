package papertoolkit.demos.biomap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import papertoolkit.PaperToolkit;
import papertoolkit.application.Application;
import papertoolkit.events.PenEvent;
import papertoolkit.events.handlers.ClickHandler;
import papertoolkit.events.handlers.InkHandler;
import papertoolkit.paper.Bundle;
import papertoolkit.paper.Region;
import papertoolkit.paper.Sheet;
import papertoolkit.paper.sheets.PDFSheet;
import papertoolkit.pen.Pen;
import papertoolkit.pen.ink.Ink;
import papertoolkit.pen.ink.InkStroke;
import papertoolkit.render.ink.InkRenderer;
import papertoolkit.units.Inches;
import papertoolkit.units.Millimeters;
import papertoolkit.units.conversion.PixelsPerInch;
import papertoolkit.util.DebugUtils;

/**
 * <p>
 * Create a GIGAprint of a JRBP Project's map. Enable some cool functionality in action bars at the top and
 * bottom of the map.
 * 
 * TODO: Fix the Pattern Rendering Bug...
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class BioMap extends Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Ask the toolkit to load the application
		// The user can then choose whether to print the GIGAprint, or run the app
		PaperToolkit r3 = new PaperToolkit();
		r3.useApplicationManager(true);
		// r3.loadApplication(new BioMap());
		r3.startApplication(new BioMap());
	}

	private AudioFeedback audioFeedback;

	private InkHandler inkWellLowerLeft;

	private InkHandler inkWellLowerRight;

	private InkHandler inkWellUpperRight;

	private int lastSite = 0;

	private Bundle notebook;

	private PDFSheet sheet;

	/**
	 * 
	 */
	public BioMap() {
		super("Field Biology Map");
		audioFeedback = new AudioFeedback();
	}

	/**
	 * Called by the super(...) constructor
	 * 
	 * @see papertoolkit.application.Application#initializeAfterConstructor()
	 */
	protected void initializeAfterConstructor() {
		// sheet = new PDFSheet(new File("data/BioMap/SurveyLocations.pdf"));
		sheet = new PDFSheet(new File("data/BioMap/SurveyLocationsLighterGaussianBlur1_0.pdf"));

		// add the three big regions
		sheet.addRegions(new File("data/BioMap/SurveyLocationsLighterGaussianBlur1_0.regions.xml"));

		Region llRegion = sheet.getRegion("LowerLeft");
		inkWellLowerLeft = new InkHandler() {
			public void handleInkStroke(PenEvent event, InkStroke mostRecentStroke) {
				DebugUtils.println("lower left");
			}
		};
		llRegion.addEventHandler(inkWellLowerLeft);

		Region lrRegion = sheet.getRegion("LowerRight");
		inkWellLowerRight = new InkHandler() {
			public void handleInkStroke(PenEvent event, InkStroke mostRecentStroke) {
				DebugUtils.println("lower right");
			}
		};
		lrRegion.addEventHandler(inkWellLowerRight);

		Region urRegion = sheet.getRegion("UpperRight");
		inkWellUpperRight = new InkHandler() {
			public void handleInkStroke(PenEvent event, InkStroke mostRecentStroke) {
				DebugUtils.println("upper right");
				audioFeedback.speak("Notes Saved for Site" + lastSite);
			}
		};
		urRegion.addEventHandler(inkWellUpperRight);
		urRegion.addEventHandler(new ClickHandler() {

			@Override
			public void clicked(PenEvent e) {
				System.out.println("Upper Right Clicked");
			}

			@Override
			public void pressed(PenEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void released(PenEvent e) {
				// TODO Auto-generated method stub

			}

		});

		// read in the BioMapSites.txt file to define the locations of the small boxed regions
		double leftStartInches = 0.96;
		double topStartInches = 0;

		final int topLeftSiteUnmarked = 1064; // 0 Inches! Everything comes from this...
		// if smaller... drop down a row
		// calculate offset in X
		// if smaller by 40, drop down another row.... and so on...

		try {
			File siteInfo = new File("data/BioMap/BioMapSites.txt");
			BufferedReader br = new BufferedReader(new FileReader(siteInfo));
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split("\t");
				System.out.println("Site #" + fields[0] + " at UTM: " + fields[1] + ", " + fields[2]);

				final int siteNum = Integer.parseInt(fields[0]);
				final float utmX = Float.parseFloat(fields[1]);
				final float utmY = Float.parseFloat(fields[2]);

				int diff = topLeftSiteUnmarked - siteNum;
				final int rowsDown = 1 + (diff / 40); // floored!
				final int colsAcross = (40 - (diff % 40)) % 40;
				System.out.println(diff + "  " + diff % 40);
				System.out.println("Rows Down: " + rowsDown + "  Cols Across: " + colsAcross);

				double xOfThisSite = leftStartInches + colsAcross * 1.18;
				double yOfThisSite = topStartInches + rowsDown * 1.18;

				// add a region
				Region rSite = new Region("Site_" + siteNum, new Inches(xOfThisSite),
						new Inches(yOfThisSite), new Inches(.85), new Inches(.85));
				rSite.addEventHandler(new ClickHandler() {

					@Override
					public void clicked(PenEvent e) {
						DebugUtils.println("Site " + siteNum + " at " + utmX + ", " + utmY);
						audioFeedback.speak("Site" + siteNum + "....");
						lastSite = siteNum;
					}

					@Override
					public void pressed(PenEvent e) {

					}

					@Override
					public void released(PenEvent e) {

					}
				});
				sheet.addRegion(rSite);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// x=18.63 inches for top row, 1.17 inches..., 6 boxes..., .85 wide x .85 tall, 0.33
		// horiz/vert between...,
		// Site 1039, 1040... 1044

		// 611 right over......... 611

		// ...

		// 491 is right over 451

		// ... 451 is right over 411

		// x=11.56 inches, for 393, 394...397, 400, 404...411

		// 354, 355, 358, 364...368

		// 315, 316

		// 275, 276, 277

		// bottom row is #236, 237, 238

		addSheet(sheet);

		// the biomap application includes one field notebook... (95 pages)
		// for now, the Application class doesn't allow us to add bundles
		// so we keep it in this subclass for now
		notebook = new Bundle("Field Notebook");
		final int numPages = 95;
		for (int i = 0; i < numPages; i++) {
			final Sheet page = new Sheet(new Millimeters(148), new Millimeters(210));
			notebook.addSheets(page); // A5
			Region region = page.createRegion();
			region.addEventHandler(getInkHandler());
		}

		// the application has to know about this pen
		// warn if there are no pens
		Pen pen = new Pen("Main Pen");
		addPenInput(pen);
	}

	private InkHandler getInkHandler() {
		return new InkHandler() {
			public void handleInkStroke(PenEvent event, InkStroke mostRecentStroke) {
				Ink inkOnThisPage = getInk();
				InkRenderer renderer = new InkRenderer(inkOnThisPage);
				// argh... we need to specify that we are rendering in dots somehow!
				// right now, we can only customize the pixels per inch....
				// TODO: FIX THIS
				int pageNumber = 1; // TODO: Should be different for each page
				renderer.renderToJPEG(new File("data/BioMap/Output/Ink_" + pageNumber + inkOnThisPage.getName() + ".jpg"),
						new PixelsPerInch(), new Millimeters(148 + 20), new Millimeters(210 + 20));
			}
		};
	}
}