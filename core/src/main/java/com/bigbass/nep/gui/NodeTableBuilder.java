package com.bigbass.nep.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.bigbass.nep.gui.Node.Tier;
import com.bigbass.nep.gui.listeners.HoverListener;
import com.bigbass.nep.recipes.Fluid;
import com.bigbass.nep.recipes.GregtechRecipe;
import com.bigbass.nep.recipes.IElement;
import com.bigbass.nep.recipes.IRecipe;
import com.bigbass.nep.skins.SkinManager;

public class NodeTableBuilder {
	
	/**
	 * Texture for the node's movement image.
	 * 
	 * Not the best way to store the asset in code, but good enough for now.
	 */
	private static TextureRegion MOVE_TEXTURE;
	
	private static final String FONTPATH = "fonts/droid-sans-mono.ttf";

	private static final Color COLOR_OVERCLOCK = new Color(0xDD1111FF);
	private static final Color COLOR_OUTPUT_HEADER = new Color(0x595959FF);
	private static final Color COLOR_INPUT_HEADER = new Color(0x595959FF);
	private static final Color COLOR_OUTPUTS_BAR = new Color(0xA6A6A6FF);
	private static final Color COLOR_INPUTS_BAR = new Color(0xD9D9D9FF);
	private static final Color COLOR_COST = new Color(0xFFC000FF);
	
	private NodeTableBuilder(){}
	
	public static void build(Node node, Table root){
		if(MOVE_TEXTURE == null){
			MOVE_TEXTURE = new TextureRegion(new Texture(Gdx.files.internal("textures/moveNode.png")));
		}
		
		root.reset();
		//root.debug();
		
		root.setWidth(240);
		
		// for organization and maintainability, each type of row is in its own private function
		nodeMenuRow(root, node);
		extraDataRow(root, node);
		titleRow(root, node);
		outputHeaderRow(root, node);
		outputRows(root, node);
		inputHeaderRow(root, node);
		inputRows(root, node);
	}
	
	private static void nodeMenuRow(Table root, Node node){
		root.row();
		final Skin rootSkin = root.getSkin();
		Table nested = new Table(rootSkin);
		
		ContainerLabel spacer = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		spacer.setBackgroundColor(Color.CLEAR);
		spacer.minWidth(root.getWidth() - 20);
		
		MoveNodeImage moveNode = new MoveNodeImage(MOVE_TEXTURE, SkinManager.getSkin(FONTPATH, 10), root);
		moveNode.setScaling(Scaling.fill);
		
		
		nested.add(spacer);
		nested.add(moveNode).width(MOVE_TEXTURE.getRegionWidth()).height(MOVE_TEXTURE.getRegionHeight());
		
		root.add(nested);
	}
	
	private static void extraDataRow(Table root, Node node){
		if(node.override != null && node.getRecipe() != null && node.getRecipe() instanceof GregtechRecipe){
			root.row();
			final Skin rootSkin = root.getSkin();
			Table nested = new Table(rootSkin);
			
			GregtechRecipe gtrec = (GregtechRecipe) node.getRecipe();
			Tier recTier = gtrec.getTier();
			
			ContainerLabel overclock = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
			overclock.label.setText("Overclock: " + recTier.name() + " >> " + node.override.name());
			overclock.label.setAlignment(Align.center);
			overclock.setBackgroundColor(COLOR_OVERCLOCK);
			overclock.setForegroundColor(COLOR_OVERCLOCK);
			overclock.minWidth(root.getWidth());
			
			
			nested.add(overclock);
			
			root.add(nested);
		}
	}
	
	private static void titleRow(Table root, Node node){
		root.row();
		final Skin rootSkin = root.getSkin();
		Table nested = new Table(rootSkin);
		
		final Color col = new Color(0xB4C2E7FF);
		
		IRecipe rec = node.getRecipe();
		
		// title
		ContainerLabel title = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		title.label.setText("Crafting Table");
		if(rec instanceof GregtechRecipe){
			title.label.setText( ((GregtechRecipe) rec).machineName );
		}
		title.setBackgroundColor(col); // temporary colors
		title.setForegroundColor(col);
		title.minWidth(root.getWidth() * 0.7f);
		
		// rate
		ContainerLabel rate = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		if(rec instanceof GregtechRecipe){
			GregtechRecipe gtrec = (GregtechRecipe) rec;
			final Tier recTier = Tier.getTier(gtrec.eut);
			
			if(node.override != null && recTier.compare(node.override) == -1){
				rate.label.setText(gtrec.getOverclockedEUt(node.override) + " EU/t");
			} else {
				rate.label.setText(gtrec.eut + " EU/t");
			}
		}
		rate.setBackgroundColor(col); // temporary colors
		rate.setForegroundColor(col);
		rate.minWidth(root.getWidth() * 0.3f);
		rate.label.setAlignment(Align.right);
		
		
		nested.add(title);
		nested.add(rate);
		
		root.add(nested);
	}
	
	private static void outputHeaderRow(Table root, Node node){
		root.row();
		final Skin rootSkin = root.getSkin();
		Table nested = new Table(rootSkin);

		IRecipe rec = node.getRecipe();
		
		// output
		ContainerLabel output = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		output.label.setText("Output:");
		output.setBackgroundColor(COLOR_OUTPUT_HEADER);
		output.setForegroundColor(COLOR_OUTPUT_HEADER);
		output.minWidth(root.getWidth() * 0.6f);
		
		// cost
		ContainerLabel cost = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		if(rec instanceof GregtechRecipe){
			GregtechRecipe gtrec = (GregtechRecipe) rec;
			if(node.override != null){
				cost.label.setText(gtrec.getTotalEU(node.override) + " EU");
			} else {
				cost.label.setText(gtrec.getTotalEU() + " EU");
			}
			cost.setBackgroundColor(COLOR_COST);
			cost.setForegroundColor(COLOR_COST);
		} else {
			cost.setBackgroundColor(COLOR_OUTPUT_HEADER);
			cost.setForegroundColor(COLOR_OUTPUT_HEADER);
		}
		cost.minWidth(root.getWidth() * 0.4f);
		cost.label.setAlignment(Align.center);
		
		
		nested.add(output);
		nested.add(cost);
		
		root.add(nested);
	}
	
	private static void outputRows(Table root, Node node){
		final Skin rootSkin = root.getSkin();

		IRecipe rec = node.getRecipe();
		
		if(rec != null){
			for(IElement el : rec.getOutput()){
				root.row();
				HoverableTable nested = new HoverableTable(rootSkin);
				
				// name
				ContainerLabel name = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
				name.label.setText(el.getLocalizedName());
				name.setBackgroundColor(COLOR_OUTPUTS_BAR);
				name.setForegroundColor(COLOR_OUTPUTS_BAR);
				name.minWidth(root.getWidth() * 0.8f);
				
				// qty
				ContainerLabel qty = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
				String qtyText = String.valueOf(el.getAmount());
				if(el instanceof Fluid){
					qtyText += "L";
				}
				qty.label.setText(qtyText);
				qty.setBackgroundColor(COLOR_OUTPUTS_BAR);
				qty.setForegroundColor(COLOR_OUTPUTS_BAR);
				qty.minWidth(root.getWidth() * 0.2f);
				qty.label.setAlignment(Align.center);
				
				nested.add(name);
				nested.add(qty).fillY();
				
				root.add(nested);
			}
		}
	}
	
	private static void inputHeaderRow(Table root, Node node){
		root.row();
		final Skin rootSkin = root.getSkin();
		Table nested = new Table(rootSkin);

		IRecipe rec = node.getRecipe();
		
		// output
		ContainerLabel input = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		input.label.setText("Input:");
		input.setBackgroundColor(COLOR_INPUT_HEADER);
		input.setForegroundColor(COLOR_INPUT_HEADER);
		input.minWidth(root.getWidth() * 0.6f);
		
		// cost
		ContainerLabel ticks = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
		if(rec instanceof GregtechRecipe){
			GregtechRecipe gtrec = (GregtechRecipe) rec;
			if(node.override != null){
				ticks.label.setText(gtrec.getOverclockedDuration(node.override) + " ticks");
			} else {
				ticks.label.setText(gtrec.duration + " ticks");
			}
		}
		ticks.setBackgroundColor(COLOR_INPUT_HEADER);
		ticks.setForegroundColor(COLOR_INPUT_HEADER);
		ticks.minWidth(root.getWidth() * 0.4f);
		ticks.label.setAlignment(Align.right);
		
		
		nested.add(input);
		nested.add(ticks);
		
		root.add(nested);
	}
	
	private static void inputRows(Table root, Node node){
		final Skin rootSkin = root.getSkin();

		IRecipe rec = node.getRecipe();
		
		if(rec != null){
			for(IElement el : rec.getInput()){
				root.row();
				HoverableTable nested = new HoverableTable(rootSkin);
				
				// name
				ContainerLabel name = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
				name.label.setText(el.getLocalizedName());
				name.setBackgroundColor(COLOR_INPUTS_BAR);
				name.setForegroundColor(COLOR_INPUTS_BAR);
				name.minWidth(root.getWidth() * 0.8f);
				
				// qty
				ContainerLabel qty = new ContainerLabel(SkinManager.getSkin(FONTPATH, 10));
				String qtyText = String.valueOf(el.getAmount());
				if(el instanceof Fluid){
					qtyText += "L";
				}
				qty.label.setText(qtyText);
				qty.setBackgroundColor(COLOR_INPUTS_BAR);
				qty.setForegroundColor(COLOR_INPUTS_BAR);
				qty.minWidth(root.getWidth() * 0.2f);
				qty.label.setAlignment(Align.center);
				
				nested.add(name);
				nested.add(qty).fillY();
				
				root.add(nested);
			}
		}
	}
	
	/**
	 * Function is called in Main.dispose()
	 */
	public static void dispose(){
		MOVE_TEXTURE.getTexture().dispose();
	}
	
	public static class MoveNodeImage extends Image {
		
		private HoverListener hoverListener;
		private DragListener dragListener;
		private Drawable hover;
		
		public MoveNodeImage(TextureRegion tex, Skin skin, Table root){
			super(tex);
			
			hover = skin.newDrawable("whiteBackground", 1, 1, 1, 0.5f);
			hoverListener = new HoverListener();
			this.addListener(hoverListener);
			
			dragListener = new DragListener(){
				
				@Override
				public void drag(InputEvent event, float x, float y, int pointer){
					root.moveBy(this.getDragX() - (tex.getRegionWidth() * 0.5f), this.getDragY() - (tex.getRegionHeight() * 0.5f));
				}
				
			};
			dragListener.setTapSquareSize(1);
			this.addListener(dragListener);
		}

		@Override
		public void draw(Batch batch, float parentAlpha){
			super.draw(batch, parentAlpha);
			if(hoverListener.isOver()){
				hover.draw(batch, getX(), getY(), getWidth(), getHeight());
			}
		}
	}
	
	public static class HoverableTable extends Table {
		
		private HoverListener hoverListener;
		private Drawable hover;
		
		public HoverableTable(Skin skin){
			super(skin);
			
			hover = skin.newDrawable("whiteBackground", 1, 1, 1, 0.5f);
			hoverListener = new HoverListener();
			this.addListener(hoverListener);
		}

		@Override
		public void draw(Batch batch, float parentAlpha){
			super.draw(batch, parentAlpha);
			if(hoverListener.isOver()){
				hover.draw(batch, getX(), getY(), getWidth(), getHeight());
			}
		}
	}
	
	public static class ContainerLabel extends CustomContainer<CustomLabel> {
		
		public CustomLabel label;
		
		public ContainerLabel(Skin skin){
			this(skin, false);
		}
		
		public ContainerLabel(Skin skin, boolean isHoverable){
			super(skin);
			
			label = new CustomLabel(skin);
			setActor(label);
			
			pad(1, 3, 1, 3);
			
			label.setWrap(true);
		}
		
		@Override
		public Container<CustomLabel> minWidth(float minWidth){
			return super.minWidth(minWidth - (this.getPadLeft() + this.getPadRight()));
		}
		
		public void setForegroundColor(Color color){
			this.label.setForegroundColor(ColorCache.getForegroundColor(color));
		}
	}
}
