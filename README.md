Introduction
============

AImage is an easy to use image management system for Android apps. We all know Android is an amazing OS, but the Android SDK makes image management (downloading, caching, resizing to fit views, etc.) very difficult; AImage does all this for you and makes it possible to do so with only a few lines of code (or by putting one of the many view types of the library in your XML layouts).

### Manifest Permissions

For disk caching, you will need the following permissions in your manifest:

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

For retrieving images over the network, you will also need these permissions in your manifest:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

Using the ImageManager
======================

The `ImageManager` is the most important class in the AImage library. It handles downloading images, and it handles
caching them on the disk and in memory so they can quickly be retrieved.

__It's recommended that you only initialize the `ImageManager` once and use the same instance everywhere, since a single
instance holds your memory cache and creating a new instance would start with a fresh memory cache (the disk cache stays, obviously).__

### Basics

Here is the most basic way to use the `ImageManager`, note that this **cannot** be used from the main UI thread. This method is
blocking, and it uses network operations to download images that aren't cached, which Android does not let you do on the main thread.

```java
ImageManager manager = new ImageManager(this);
Bitmap jbLogo = manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", null);
// ...do something with the downloaded Bitmap
```

### Resizing images

AImage makes it very easy to down sample images.

```java
//The downloaded image will be 30dp by 50dp
Dimension dimenDp = new Dimension(this, 30.0f, 50.0f);
ImageManager manager = new ImageManager(this);
Bitmap jellybean = manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", dimenDp);
// ...do something with the downloaded Bitmap
```

There's other ways of initializing the `Dimension` class to make it easier to get what you need.

```java
// 50dp by 50dp (passing a context and using a float value indicates dp)
Dimension dimenSquaredDp = new Dimension(this, 50.0f);

// 50dp by 20dp (passing a context and using float values indicates dp)
Dimension dimenDp = new Dimension(this, 50.0f, 20.0f);

// 50px by 50px (passing no context and an integer indicates pixels)
Dimension dimenSquaredPx = new Dimension(50);

// 30px by 50px (passing no context and an integer indicates pixels)
Dimension dimenPx = new Dimension(30, 50);
```

### Asynchronous Loading

AImage makes it easy to load images on a separate thread. Results are posted to a callback where
you can do whatever you want with the image.

```java
ImageManager manager = new ImageManager(this);

// Replace the `null` parameter with a `Dimension` instance if you want to down sample (like in the above example)
manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", null, new ImageListener() {
    @Override
    public void onImageReceived(String source, Bitmap bitmap) {
        // ...do something with the downloaded Bitmap
    }
});
```

Again, if you want to down sample the image, you can pass an instance of the `Dimension` class for the second parameter to `get()`.

### Changing the Cache Directory

By default, AImage uses your app's external cache directory to cache images, which is usually in a location similar to
`/sdcard/Android/data/com.example.package_name/cache`. This cache directory can also be cleared from the Android's App Info screen.
If you want to change the cache directory that's used, it's pretty easy:

```java
ImageManager manager = new ImageManager(context);

// This is the default cache directory
manager.setCacheDirectory(context.getExternalCacheDir());

// This will set it to the directory `/sdcard/My Directory`
manager.setCacheDirectory(new File(Environment.getExternalStorageDirectory(), "My Directory"));
```

### Changing the Fallback Image

If you want AImage to return an image in the case that an image fails to load, you can specify a fallback image that will allow
just that.

```java
ImageManager manager = new ImageManager(this);
// Replace parameter with a drawable resource ID of your choice
manager.setFallbackImage(R.drawable.fallback_image);
```

This most common case that this would be useful is if you're loading images from the network. If an image fails to download
(if it's not cached and you don't have a connection, the server fails to respond, etc.), then the fallback image will be used.

Views
======

### AImageView

Using the library is even easier when you use the `AImageView` in your XML layouts. Not only does it handle interacting with
the `ImageManager`, it also waits until it's been measured (which isn't immediately when your app starts) and loads images
to fit its own dimensions.

The `AImageView` takes precautions so that it doesn't download and display the wrong image when you're
scrolling quickly in a ListView (which is a common problem for people that have their own image management implementations on Android);
this problem is caused by views being recycled and re-used.

To implement the `AImageView` in your XML layout, just replace `ImageView` with `com.afollestad.aimage.views.AImageView` like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<com.afollestad.aimage.views.AImageView
            android:id="@+id/imageView"
            android:scaleType="fitXY"
            android:layout_width="120dp"
            android:layout_height="120dp" />
```

And implement code like this:

```java
ImageManager manager = new ImageManager(this);

//Replace the view ID with whatever ID you're using in your XML layout
AImageView aview = (AImageView)findViewById(R.id.image);

aview.setManager(manager)
     .setSource("http://www.android.com/images/whatsnew/jb-new-logo.png")
     .setFitView(true)  // This is already set to true by default, sets whether or not the image will be resized to fit the view
     .load();
````

__Again, it's recommended that you only initialize the `ImageManager` once and use the
same instance everywhere, since a single instance holds your memory cache and creating a new instance would start with a)
fresh memory cache (the disk cache stays, obviously).__

### AspectAImageView

The `AspectAImageView` is the same as the `AImageView` class, but it automatically adjusts its height to keep aspect ratio with
the image's width (even when the view is in a `RelativeLayout` and you're using `FILL_PARENT`, `MATCH_PARENT`, or `WRAP_CONTENT` for dimensions).

All that you have to do is replace `AImageView` with `AspectAImageView` in your layouts and code, and we'll take care of
the rest.

### SquareAImageView

The `SquareAImageView` is the same as the `AImageView` class, but it automatically adjusts the height to always equal
the width of the view, making it a perfect square. It's like dragging the bottom right corner of a window to resize both
the width and height of a window.

All that you have to do is replace `AImageView` with `SquareAImageView` in your layouts and code, and we'll take care of
the rest.

### HeightSquareAImageView

The `HeightSquareAImageView` is the same as the `SquareAImageView` class, but instead of adjusting the height to always equal
the width, the width is automatically adjusted to equal the height of the view.

All that you have to do is replace `AImageView` with `HeightSquareAImageView` in your layouts and code, and we'll take care of
the rest.

### AImageContactBadge

The `AImageContactBadge` is basically a `QuickContactBadge` (from the stock SDK) version of the `AImageView`. It allows you to assign an email address or phone number to show a contact badge when the view is pressed. It also allows use of `setImageToDefault()`, which shows a default silhouette image (this replaces the fallback image functionality of the regular `AImageView`).