package danbroid.mopidy.lastfm;

import java.io.IOException;

/**
 * Created by dan on 21/12/17.
 */
public class Test {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class);



	@org.junit.Test
	public void test1() throws IOException {
		log.debug("test1()");

		new AlbumSearch()
				.album("Highway To Hell")
				.artist("AC/DC")
				.call();

	}

	@org.junit.Test
	public void test2() throws IOException {
		log.debug("test2()");

		Response response = new AlbumSearch()
				.album("Space Oddity")
				.artist("David Bowie")
				.call();

		log.debug("image: " + response.album.getImage(Album.ImageSize.DEFAULT));


	}

	@org.junit.Test
	public void test3() throws IOException {
		log.debug("test3()");

		Response response = new AlbumSearch()
				.album("Singles")
				.artist("Future Islands")
				.call();

		log.debug("image: " + response.album.getImage(Album.ImageSize.DEFAULT));
		for (Album.Tag tag : response.album.tags.tag) {
			log.trace("tag: {}", tag.name);
		}

		log.debug("summary: " + response.album.wiki.summary);


	}
}
