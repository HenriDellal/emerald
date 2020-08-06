# Emerald Launcher [![codebeat badge](https://codebeat.co/badges/99f8e462-4277-422f-a077-72769c740a45)](https://codebeat.co/projects/github-com-henridellal-emerald-master)

Emerald is a simple home screen for Android phones. It aims good performance while providing basic customization support.

## Downloads
[F-Droid](https://f-droid.org/packages/ru.henridellal.emerald)

[Google Play](https://play.google.com/store/apps/details?id=ru.henridellal.emerald)

## Features
- Icon pack support;
- App and web search;
- App categories;
- History of last launched apps;
- Resizeable layout.

## To do
- Oreo+ shortcuts and package service;
- Some bug fixes and improvements.

## How to use
- Swipe left or right to switch categories;
- Tap on category name to see the list of categories.

## How to compile
There are multiple options:
- Gradle;
- In Termux:

Some packages should be installed

`$ pkg install aapt apksigner dx`

If your Android version is 7.0 or higher

`$ pkg install ecj`

Otherwise

`$ pkg install ecj4.6`

Then navigate to the emerald project folder and run the script (it is recommended to edit paths in it first)

```
$ chmod u+x termux-build.sh
$ ./termux-build.sh
```

## Questions and answers
##### I've disabled the main bar and can't access settings now. How do I open them?
There are some methods to open settings without the main bar:
- Use Launcher Settings shortcut (pre-Oreo only).
- Press Recents/Menu button on the navigation bar(provided by system). Some devices may have different ways (For example, holding Back button on Galaxy phones).
- Use [ActivityLauncher](https://github.com/butzist/ActivityLauncher). Choose All activities > Emerald Launcher > .Options
##### Is it possible to use Emerald as an app drawer?
Yes.
##### Will Emerald Launcher support widgets?
The launcher is most likely not to introduce widgets in future updates.
##### Does it support physical keyboards?
Partially. It isn't possible to do some actions (e.g. launch apps from menu), though some are available with keyboard shortcuts:
- Alt + number to launch n-th app/shortcut from dock.
- Alt + Up to open the categories list.
- Alt + Left/Right to switch categories.
