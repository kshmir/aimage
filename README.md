# Introduction

AImage is an easy to use image manager system for Android apps. We all know Android is an amazing OS, but the Android SDK
makes image management (downloading, caching, resizing to fit views, etc.) very difficult; AImage does all this for you
and makes it possible to do so with only a few lines of code (or by putting an AImageView or AspectAImageView in your XML
layouts.

## Using the ImageManager

The ImageManager is the most important class in the AImage library. It handles downloading images, and it handles
caching them on the disk and in memory so they can quickly be retrieved.

### Basics

Here is the most basic way to use the ImageManager, note that this can NOT be used from the main UI thread; this
way of using the manager is blocking and Android does not let you use the network from the UI thread.

```java
ImageManager manager = new ImageManager(this);
Bitmap jbLogo = manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", null);
// ...do something with the downloaded Bitmap
```

### Resizing images

AImage makes it very easy to down sample images.
                                                                                                ava
```java
//The downloaded image will be 30dp by 50dp
Dimension dimenDp = new Dimension(this, 30.0f, 50.0f);
ImageManager manager = new ImageManager(this);
Bitmap jellybean = manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", dimenDp);
// ...do something with the downloaded Bitmap
```

There's other ways of initializing the Dimension class to make it easier.

```java
// 50dp by 50dp (using a float value indicates dp)
Dimension dimenSquaredDp = new Dimension(this, 50.0f);

//50px by 50px (passing no context and an integer indicates pixels)
Dimension dimenSquaredPx = new Dimension(50);

//30px by 50px (passing no context and an integer indicates pixels)
Dimension dimenPx = new Dimension(30, 50);
```

### Asynchronous Loading

AImage makes it easy to load images on a separate thread, and return the results to a callback where
you can do whatever you want with the image.

```java
ImageManager manager = new ImageManager(this);
manager.get("http://www.android.com/images/whatsnew/jb-new-logo.png", null, new ImageListener() {
    @Override
    public void onImageReceived(String source, Bitmap bitmap) {
        // ...do something with the downloaded Bitmap
    }
});
```

Again, if you want to down sample the image, you can pass an instance of the Dimension class for the second parameter to get().