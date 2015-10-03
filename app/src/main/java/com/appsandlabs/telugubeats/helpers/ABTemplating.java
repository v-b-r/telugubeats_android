package com.appsandlabs.telugubeats.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.activities.R;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

import static com.appsandlabs.telugubeats.helpers.UiUtils.getColorFromResource;

/*
 *  Render json templating model to help create clean ui's without a solid plan ,
 *  just know what data you need to display, and how much space they need , render json will auto compute the layout
 *  You can also write your templates in easy on-the-go fashion and no really give a thought. 
 *  No xml here , so its a little pain on creating the elements yourself and manipulation, but hey , it's programming after all.
 */
public class ABTemplating {


	private static final int CENTER_VERTICAL = Gravity.CENTER_VERTICAL;
	private static final int RIGHT = Gravity.RIGHT;
	private static final int CENTER_HORIZONTAL = Gravity.CENTER_HORIZONTAL;

	static final int MATCH_PARENT = LayoutParams.MATCH_PARENT;
	static final int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;
//
//	public enum Ids {
//		PLAYER_AND_STATS,
//		NONE ,
//		ACTORS,
//		DIRECTORS ,
//		SINGERS,
//		PLAYING_IMAGE, LIVE_USERS, WHATS_APP_DEDICATE, SCROLLING_DEDICATIONS, NEXT_POLLS_HEADING;
//
//		public String getString() {
//			return this.name().toLowerCase() + "_container";
//		}
//	}


	public final static int NONE = -5;


	public enum ViewType {
		SCROLL_VIEW,
		NORMAL, HORIZONTAL_SCROLL_VIEW
	}

	public static class ABView extends LinearLayout {
		private HashMap<String, ABView> cells = new HashMap<String, ABView>();
		private String name = null;

		ViewType viewType = ViewType.NORMAL;
		private Object tag2;
		private TextView label;
		private ImageView iconView;
		private ImageView imageView;
		private int height = LayoutParams.WRAP_CONTENT;
		private int width =  LayoutParams.WRAP_CONTENT;

		public ABView(String id) {
			super(TeluguBeatsApp.getContext(), null);
			if (Config.IS_TEST_BUILD)
				UiUtils.setBg(this, getContext().getResources().getDrawable(R.drawable.custom_border));
			name = id;
			cells.put(name, this);
		}

		float oneDp = -1;
		public float getInDp(int i) {
			if(oneDp==-1){
				oneDp = getContext().getResources().getDimension(R.dimen.one_dp);
			}
			return i*oneDp;
		}

		float oneSp = -1;
		public float getInSp(int i) {
			if(oneSp==-1){
				oneSp = getContext().getResources().getDimension(R.dimen.one_sp);
			}
			return i*oneSp;
		}

		@SuppressLint("NewApi")
		public ABView(int styleResource) {
			super(TeluguBeatsApp.getContext(), null, styleResource);
			if (Config.IS_TEST_BUILD)
			UiUtils.setBg(this, getContext().getResources().getDrawable(R.drawable.custom_border));
		}

		@SuppressLint("NewApi")
		public ABView(int styleResource, String id) {
			super(TeluguBeatsApp.getContext(), null, styleResource);
			if (Config.IS_TEST_BUILD)
				UiUtils.setBg(this, getContext().getResources().getDrawable(R.drawable.custom_border));
			name = id;
			cells.put(name, this);
		}

		public ABView( ABView... views) {
			super(TeluguBeatsApp.getContext());
			if (Config.IS_TEST_BUILD)
				UiUtils.setBg(this, getContext().getResources().getDrawable(R.drawable.custom_border));

			registerInnerViews(views);
		}



		public void registerInnerView(ABView view) {
			cells.putAll(view.getAllCells());
		}

		public void registerInnerViews(ABView[] views) {
			for (ABView view : views) {
				cells.putAll(view.getAllCells());
			}
		}


		public HashMap<String, ABView> getAllCells() {
			return cells;
		}

		public ABView getCell(String cellName) {
			return cells.get(cellName);
		}

		public ABView setCell(String cellName, ABView view) {
			return cells.put(cellName, view);
		}

		public ABView gty(int gravity) {
			this.setGravity(gravity);
			return this;
		}

		public ABView lgty(int gravity) {
			if(this.getLayoutParams()!=null)
				((LayoutParams)this.getLayoutParams()).gravity = gravity;
			return this;
		}

		public ABView wd(int width) {
			if(width>0)
				width = (int)getInDp(width);

			if (this.getLayoutParams() == null)
				this.setLayoutParams(new LayoutParams(width, height));
			else
				((LayoutParams) this.getLayoutParams()).width = width;

			this.width = width;
			return this;
		}

		public ABView sz(int width, int height) {
			return wd(width).ht(height);
		}

		public ABView occupy() {
			return wd(MATCH_PARENT).ht(MATCH_PARENT);
		}


		public ABView ht(int height) {
			if(height>0)
				height = (int)getInDp(height);
			if (this.getLayoutParams() == null)
				this.setLayoutParams(new LayoutParams(width , height));
			else
				((LayoutParams) this.getLayoutParams()).height = height;
			this.height = height;
			return this;
		}

		public ABView wgt(float weight) {
			if (this.getLayoutParams() == null)
				this.setLayoutParams(new LayoutParams(0, height));

			((LayoutParams) this.getLayoutParams()).width = 0;
			((LayoutParams) this.getLayoutParams()).weight = weight;
			return this;
		}

		public ABView wgtSum(int i) {
			setWeightSum(i);
			return this;
		}


		public ABView addLabel(String string) {
			return addLabel(string, -1, false) ;
		}
		public ABView addLabel(String string, boolean isHeading) {
			return addLabel(string, -1, true) ;
		}

		public ABView txtColor(int color){
			if(label !=null){
				label.setTextColor(color);
			}
			return this;
		}

		public ABView txtSize(float i) {
			if(label !=null){
				label.setTextSize(i);
			}
			return this;
		}


		public ABView asSubTitle(){
			if(label != null) {
				label.setTextColor(Color.LTGRAY);
				label.setTextSize(10);
			}
			return this;
		}

		public ABView asHeading(){
			if(label != null) {
				setPadding(0 , 20 , 0 , 20);
				label.setTextColor(Color.BLACK);
				label.setTextSize(15);
				label.setTypeface(null, Typeface.BOLD);
			}
			return this;
		}


		public ABView asTitle(){
			if(label != null) {
				label.setTextColor(getResources().getColor(R.color.black));
				label.setTextSize(13);
				label.setTypeface(null, Typeface.BOLD);
			}
			return this;
		}

		public ABView asDescription(){
			if(label != null) {
				label.setTextColor(getResources().getColor(R.color.less_black));
				label.setTextSize(13);
			}
			return this;
		}

		public ABView addLabel(String string, float textSizeSp, boolean bold) {
			if(label==null) {
				label = new TextView(getContext(), null);
				super.addView(label);
			}
			label.setText(string);
			if(textSizeSp!=-1)
				label.setTextSize(textSizeSp);
			else{
				label.setTextSize(12);
			}
			this.setPadding(5, 0, 0, 5);
			if(bold) {
				label.setTypeface(null, Typeface.BOLD);
			}
			label.setTextColor(Color.BLACK);
			this.setGravity(Gravity.CENTER_VERTICAL);
			return this;
		}

		public TextView getLabel() {
			return label;
		}


		@Override
		protected void removeDetachedView(View child, boolean animate) {
			super.removeDetachedView(child, false);
		}


		public ABView asVScrollView(ABView... views) {
			ABView scrollChild = new ABView( name);
			scrollChild.setOrientation(LinearLayout.VERTICAL);
			scrollChild.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			ScrollView scrollView = new ScrollView(getContext());
			scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			scrollView.addView(scrollChild);
			this.addView(scrollView);
			this.viewType = ViewType.SCROLL_VIEW;
			//add all views inside this to scroller
			if (views.length > 0) {
				for (ABView view : views) {
					this.addView(view);
				}
			}
			return this;
		}

		public ABView asHScrollView(ABView... views) {
			ABView scrollChild = new ABView(name);
			scrollChild.setOrientation(LinearLayout.HORIZONTAL);
			scrollChild.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
			//scrollView.setScrollBarSize(0);
			scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			scrollView.addView(scrollChild);
			this.addView(scrollView);
			this.viewType = ViewType.HORIZONTAL_SCROLL_VIEW;
			//add all views inside this to scroller
			if (views.length > 0) {
				for (ABView view : views) {
					this.addView(view);
				}
			}
			return this;
		}

		@Override
		public void addView(View child) {
			switch (viewType) {
				case SCROLL_VIEW:
					((ABView) ((ScrollView) this.getChildAt(0)).getChildAt(0)).addView(child);
					break;
				case HORIZONTAL_SCROLL_VIEW:
					((ABView) ((HorizontalScrollView) this.getChildAt(0)).getChildAt(0)).addView(child);
					break;
				default:
					if(child.getLayoutParams()==null)
						child.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
					super.addView(child);
			}
		}

		public ABView addView(ABView child) {
			registerInnerView(child);
			switch (viewType) {
				case SCROLL_VIEW:
					((ABView) ((ScrollView) this.getChildAt(0)).getChildAt(0)).addView(child);
					break;
				case HORIZONTAL_SCROLL_VIEW:
					((ABView) ((HorizontalScrollView) this.getChildAt(0)).getChildAt(0)).addView(child);
					break;
				default:
					super.addView(child);
			}
			return this;
		}

		public ABView addView(ABView child, int index) {
			// TODO Auto-generated method stub
			registerInnerView(child);
			switch (viewType) {
				case SCROLL_VIEW:
					((ABView) ((ScrollView) this.getChildAt(0)).getChildAt(0)).addView(child, index);
					break;
				default:
					super.addView(child, index);
			}

			return this;
		}

		public void removeView(int index) {
			switch (viewType) {
				case SCROLL_VIEW:
					((ABView) ((ScrollView) getChildAt(0)).getChildAt(0)).removeView(index);
					break;
				case HORIZONTAL_SCROLL_VIEW:
					((ABView) ((HorizontalScrollView) getChildAt(0)).getChildAt(0)).removeView(index);
					break;
				default:
					super.removeViewAt(index);
			}
			return;
		}

		public void removeAllViews() {
			switch (viewType) {
				case SCROLL_VIEW:
					((ABView) ((ScrollView) getChildAt(0)).getChildAt(0)).removeAllViews();
					break;
				case HORIZONTAL_SCROLL_VIEW:
					((ABView) ((HorizontalScrollView) getChildAt(0)).getChildAt(0)).removeAllViews();
					break;
				default:
					super.removeAllViews();
			}
			return;
		}

		public ABView getScrollChildAt(int index) {
			switch (viewType) {
				case SCROLL_VIEW:
					return (ABView) ((ABView) ((ScrollView) getChildAt(0)).getChildAt(0)).getChildAt(index);
				case HORIZONTAL_SCROLL_VIEW:
					return (ABView) ((ABView) ((HorizontalScrollView) getChildAt(0)).getChildAt(0)).getChildAt(index);
				default:
					break;
			}
			return (ABView) super.getChildAt(index);
		}

		public ABView underline() {
			View v = new View(getContext());
			v.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
			v.setBackgroundColor(Color.GRAY);
			this.addView(v);
			return this;
		}


		public void setTag2(Object pendingJob) {
			tag2 = pendingJob;
		}


		public Object getTag2() {
			return tag2;
		}




		public ABView setIcon(UiUtils.Images img) {
			return setIcon(img, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		public ABView setIcon(UiUtils.Images img, int width , int height) {
			iconView = new ImageView(getContext());
			if(width>0 && height>0)
				iconView.setLayoutParams(new LayoutParams((int)getInDp(width), (int)getInDp(height)));
			else
				iconView.setLayoutParams(new LayoutParams(width, height));
			((LayoutParams)iconView.getLayoutParams()).gravity = Gravity.CENTER;
			this.addView(iconView);
			if(img.getValue()!=null)
				UiUtils.loadImageIntoView(getContext(), iconView, img.getValue(), false, -2 , -2,null);
			else if(img.getResourceId()!=-1){
				UiUtils.setBg(iconView, getContext().getResources().getDrawable(img.getResourceId()));
			}
			return this;
		}

		public ABView asImage() {
			if(imageView == null)
				imageView = new ImageView(getContext());
			this.addView(imageView);
			return this;
		}

		public ImageView getImage(){
			return imageView;
		}

		public ABView setBg(UiUtils.Images img) {
			UiUtils.setBg(this, img.getDrawable(this.getContext()));
			return this;
		}

		public ABView setBg(int drawableResource) {
			UiUtils.setBg(this, getContext().getResources().getDrawable(drawableResource));
			return this;

		}
		public ABView setBg(String assetPath) {
			UiUtils.loadImageAsBg(this, assetPath, false);
			return this;
		}

		public ABView padding(int left, int top, int right, int bottom) {
			this.setPadding(left, top, right, bottom);
			return this;
		}

		public ABView margin(int left, int top, int right, int bottom) {
			LayoutParams layoutParams = ((LayoutParams) this.getLayoutParams());
			if(layoutParams == null){
				layoutParams = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
				setLayoutParams(layoutParams);
			}
			layoutParams.setMargins(left, top, right, bottom);
			return this;
		}

		public ABView setBgColor(int color) {
			this.setBackgroundColor(color);
			return this;
		}

		public int getThemeColor() {
			return Color.GRAY;
		}

		public ABView margin(int i) {
			return margin(i , i , i , i );
		}
	}

	Context ctx = null;

    public ABTemplating(Context ctx) {
        this.ctx = ctx;


    }

    public Button createButton(String title, String style, int width, int height) {
        return null;
    }

    public void createRadio() {

    }

    public void createDropDown() {

    }

    public void createRowGrid() {

    }
    /*
    horizontal viewers
     */
    public ABView h(String id, ABView... views) {
        ABView ret = new ABView(id );
        return _h(ret, views);
    }

    public ABView h(String id, int style, ABView... views) {
        ABView ret = new ABView(style , id);
        return _h(ret, views);
    }


    public ABView h(boolean isScroll, String id , ABView... views) {
        ABView ret = null;
        if (isScroll)
            ret = new ABView(id).asHScrollView();
        else
            ret = new ABView(id);

        return _h(ret, views);
    }

    public ABView h( ABView... views) {
        ABView ret = new ABView("none" );
        return _h(ret, views);
    }

    public ABView h(int style, ABView... views) {
        ABView ret = new ABView(style , "none");
        return _h(ret, views);
    }


    public ABView h(boolean isScroll, ABView... views) {
        ABView ret = null;
        if (isScroll)
            ret = new ABView("none").asHScrollView();
        else
            ret = new ABView("none");

        return _h(ret, views);
    }

    private ABView _h(ABView ret, ABView... views) {

        ret.setOrientation(LinearLayout.HORIZONTAL);
//			ret.registerInnerViews(views); //add view registers it now
        for (ABView view : views) {
            if (view.getLayoutParams() != null && ((LayoutParams) view.getLayoutParams()).weight == 0 && view.width==0)// if weight set , we had set its width to 0 , hence don't set
                view.wd(LayoutParams.WRAP_CONTENT);
            ret.addView(view);
        }
        return ret;
    }

    // horizontal views end

    public ABView v(boolean isScroll, ABView... views) {
        return v(isScroll, "none", views);
    }
    public ABView v(ABView... views){
        return v("none", views);
    }
    public ABView v(int style, ABView... views) {
        return v(style, "none", views);
    }

    public ABView v(boolean isScroll, String id, ABView... views) {
        ABView ret = null;
        if (isScroll)
            ret = new ABView(id).asVScrollView();
        else
            ret = new ABView(id);

        return _v(ret, views);
    }


    public ABView v(String id, ABView... views) {
        ABView ret = new ABView(id);
        return _v(ret, views);
    }

    public ABView v(int style, String id, ABView... views) {
        ABView ret = new ABView(style, id);
        return _v(ret, views);
    }


	public FrameLayout l(ABView ... views){
		FrameLayout frameLayout = new FrameLayout(TeluguBeatsApp.getContext());
		frameLayout.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
		for(ABView view: views){
			ViewGroup.LayoutParams lParams = view.getLayoutParams();
			if(lParams!=null)
				view.setLayoutParams(new FrameLayout.LayoutParams(lParams.width, lParams.height));
			frameLayout.addView(view);
		}
		return frameLayout;
	}

    private ABView _v(ABView ret, ABView... views) {
		if(views!=null) {
			ret.setOrientation(LinearLayout.VERTICAL);
			//		ret.registerInnerViews(views); //add view registers it now
			for (ABView view : views) {
				view.wd(LayoutParams.MATCH_PARENT);
				ret.addView(view);
			}
		}
		ret.wd(LayoutParams.MATCH_PARENT);
		return ret;
	}


	public Object loadViewAsObject(ABView view, Class<?> clazz) {
		HashMap<String, ABView> cellNames = view.getAllCells();
		try {
			Object object = clazz.newInstance();
			for (Entry<String, ABView> entry : cellNames.entrySet()) {
				set(object, entry.getKey(), entry.getValue());
			}
			return object;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean set(Object object, String fieldName, Object fieldValue) {
		Class<?> clazz = object.getClass();
		while (clazz != null) {
			try {
				Field field = clazz.getDeclaredField(fieldName + "Holder");
				field.setAccessible(true);
				field.set(object, fieldValue);
				return true;
			} catch (NoSuchFieldException e) {
				clazz = clazz.getSuperclass();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return false;
	}

	public ABView c(String id){
		return new ABView(id);
	}
	public ABView c(){
		return new ABView("none");
	}

	public FrameLayout getMusicAndEventsView(){
		return l(

				v(true,
								c("scrolling_dedications").ht(50),
								h(c().addLabel("Singers").wgt(0.3f).asTitle(), c("singers").wgt(0.7f).addLabel("").asDescription()).margin(0, 30, 0, 0),
								h(c().addLabel("Actors").wgt(0.3f).asTitle(), c("actors").wgt(0.7f).addLabel("").asDescription()),
								h(c().addLabel("Director").wgt(0.3f).asTitle(), c("directors").wgt(0.7f).addLabel("").asDescription()),
								c().underline(),
								h(c("live_users").addLabel("1000 live users").asHeading().wgt(0.5f).margin(10), c("whats_app_dedicate").wgt(0.5f)),
								c("visualizer").margin(0, 10, 0, 20).setBgColor(getColorFromResource(R.color.default_bg)),
								c("playingImage").wd(MATCH_PARENT)
						).occupy().padding(5, 10, 5, 0).setBgColor(getColorFromResource(R.color.translucent_white))
		);
	}

	public ABView getPollsView(){
		return v(c("live_polls_heading").addLabel("Live polls for next song").asHeading().gty(CENTER_HORIZONTAL),
				c("live_polls_list")).occupy().setBgColor(Color.WHITE);

	}

	public ABView getPollView() {
		return v(
				h(c("poll_image").asImage().sz(60, 60), v(
								h(c("poll_title").addLabel(""), c("voted").addLabel(" ").sz(10, 10).lgty(CENTER_VERTICAL).margin(20, 0, 0, 0)),
								h(c("poll_subtitle").addLabel("").asSubTitle().wgt(0.5f) ,  c("poll_subtitle2").addLabel("").asSubTitle().wgt(0.5f)),
								c("poll_subtitle3").addLabel("").asSubTitle()
								).margin(10, 0, 0, 0)
						),
						h(c("poll_percentage").ht(5).lgty(CENTER_VERTICAL),
								c("poll_count").addLabel("").txtSize(14).lgty(CENTER_VERTICAL)
						).ht(25)
				).setBg(R.drawable.card).padding(20, 10, 20, 10);
	}

}
	