package com.sesu8642.feudaltactics.ui.stages.slidestage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.viewport.Viewport;

public class SlideStage extends Stage {

	private List<Table> slides;

	Set<Disposable> disposables = new HashSet<Disposable>();
	private Skin skin;
	private OrthographicCamera camera;
	private Table rootTable;
	private Table currentSlide;
	private TextButton backButton;
	private TextButton nextButton;
	private ScrollPane scrollPane;
	private Stack slideAreaStack;
	private Container<Table> slideContainer = new Container<Table>();

	public SlideStage(Viewport viewport, List<Slide> slides, Runnable finishedCallback, OrthographicCamera camera,
			Skin skin) {
		super(viewport);
		if (slides.isEmpty()) {
			throw new IllegalArgumentException("at least one slide is required");
		}
		this.camera = camera;
		this.skin = skin;
		this.slides = slides.stream().map(s -> s.getTable()).collect(Collectors.toList());
		initUI(this.slides, finishedCallback);
	}

	private void initUI(List<Table> slides, Runnable finishedCallback) {
		backButton = new TextButton("", skin);
		backButton.setDisabled(true);
		backButton.setTouchable(Touchable.disabled);

		nextButton = new TextButton("", skin);

		currentSlide = slides.get(0);

		TextArea backgroundArea = new TextArea(null, skin);
		backgroundArea.setDisabled(true);

		slideContainer.fill();
		slideContainer.pad(20, 25, 20, 20);
		slideContainer.setActor(currentSlide);

		slideAreaStack = new Stack(backgroundArea, slideContainer);

		scrollPane = new ScrollPane(slideAreaStack, skin);
		scrollPane.setFadeScrollBars(false);
		scrollPane.setOverscroll(false, false);

		rootTable = new Table();
		rootTable.setFillParent(true);
		rootTable.defaults().minSize(0);
		rootTable.add(scrollPane).expand().fill().colspan(2);
		rootTable.row();
		rootTable.defaults().minHeight(100).pad(0).expandX().bottom().fillX();
		rootTable.add(backButton);
		rootTable.add(nextButton);

		this.addActor(rootTable);

		nextButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int currentSlideIndex = slides.indexOf(currentSlide);
				if (slides.size() > currentSlideIndex + 1) {
					Table newSlide = slides.get(currentSlideIndex + 1);
					slideContainer.setActor(newSlide);
					currentSlide = newSlide;
					if (slides.size() == currentSlideIndex + 2) {
						nextButton.setText("Finish");
					}
					backButton.setTouchable(Touchable.enabled);
					backButton.setDisabled(false);
					backButton.setText("Back");
					camera.update();
				} else {
					finishedCallback.run();
				}
			}
		});

		backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				int currentSlideIndex = slides.indexOf(currentSlide);
				if (currentSlideIndex > 0) {
					Table newSlide = slides.get(currentSlideIndex - 1);
					slideContainer.setActor(newSlide);
					currentSlide = newSlide;
					nextButton.setText("Next");
					if (currentSlideIndex == 1) {
						backButton.setTouchable(Touchable.disabled);
						backButton.setDisabled(true);
						backButton.setText("");
					}
				}
			}
		});
	}

	public void reset() {
		backButton.setTouchable(Touchable.disabled);
		backButton.setDisabled(true);
		backButton.setText("");
		String nextButtonText = slides.size() == 1 ? "Finish" : "Next";
		nextButton.setText(nextButtonText);
		System.out.println(slides == null);
		currentSlide = slides.get(0);
		slideContainer.setActor(currentSlide);
	}

	public void updateOnResize(int width, int height) {
		camera.viewportHeight = height;
		camera.viewportWidth = width;
		camera.update();
		rootTable.pack();
		slides.forEach(slide -> {
			slide.pack();
			slide.getChildren().forEach(child -> {
				if (ClassReflection.isAssignableFrom(Table.class, child.getClass())) {
					((Table) child).pack();
				}
			});
		});
	}

	@Override
	public void draw() {
		super.draw();
	}

	@Override
	public void dispose() {
		super.dispose();
		for (Disposable disposable : disposables) {
			disposable.dispose();
		}
	}
}