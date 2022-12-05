<!-- omit in toc -->
# Contributing to CustomCobbleGen

First off, thanks for taking the time to contribute! ❤️

All types of contributions are encouraged and valued. See the [Table of Contents](#table-of-contents) for different ways to help and 
details about how this project handles them. Please make sure to read the relevant section before making your contribution. It will 
make it a lot easier for us maintainers and smooth out the experience for all involved. The community looks forward to your 
contributions. 🎉

> And if you like the project, but just don't have time to contribute, that's fine. There are other easy ways to support the project 
and show your appreciation, which we would also be very happy about:
> - Star the project
> - Leave a review on Spigot

<!-- omit in toc -->
## Table of Contents

- [I Have a Question](#i-have-a-question)
- [I Want To Contribute](#i-want-to-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Your First Code Contribution](#your-first-code-contribution)


## I Have a Question

> If you want to ask a question, we assume that you have read the available 
[Documentation](https://github.com/PhilipFlyvholm/CustomCobbleGen).

To make the development of CustomCobbleGen as smooth as possible has it been choosen to split the support up in two. If you have a config related problem then [join the discord support server](https://discord.gg/6UpwEDUm6V) to get a quick response.

If you on the otherhand have found a bug or have an idea for a future feature then you can use [Github 
Issues](https://github.com/PhilipFlyvholm/CustomCobbleGen/issues). This will make it easy for everyone to see which bugs or feature request to work on.
Before you ask a question, it is best to search for existing [Issues](https://github.com/PhilipFlyvholm/CustomCobbleGen/issues) that might help you. (It will be marked as duplicated and ignored otherwise)

We will then take care of the issue as soon as possible.

## I Want To Contribute

> ### Legal Notice
> When contributing to this project, you must agree that you have authored 100% of the content, that you have the necessary rights to the content and that the content you contribute may be provided under the project license.

### Reporting Bugs

<!-- omit in toc -->
#### Before Submitting a Bug Report

A good bug report shouldn't leave others needing to chase you up for more information. Therefore, we ask you to investigate 
carefully, collect information and describe the issue in detail in your report. Please complete the following steps in advance to 
help us fix any potential bug as fast as possible.

- Make sure that you are using the latest version.
- Determine if your bug is really a bug and not an error on your side e.g. a config error (Make sure that you have read the 
[documentation](https://github.com/PhilipFlyvholm/CustomCobbleGen). If you are looking for support, you might want to check [this 
section](#i-have-a-question)).
- To see if other users have experienced (and potentially already solved) the same issue you are having, check if there is not 
already a bug report existing for your bug or error in the [bug tracker](https://github.com/PhilipFlyvholm/CustomCobbleGen/issues?q=label%3Abug).
Make sure to follow the templates given as it makes the process more smooth :D

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion for CustomCobbleGen, **including completely new features and minor improvements to existing functionality**. Following these guidelines will help maintainers and the community to understand your suggestion and find related suggestions.

<!-- omit in toc -->
#### Before Submitting an Enhancement

- Make sure that you are using the latest version.
- Read the [documentation](https://github.com/PhilipFlyvholm/CustomCobbleGen) carefully and find out if the functionality is already covered, maybe by an individual configuration.
- Perform a [search](https://github.com/PhilipFlyvholm/CustomCobbleGen/issues) to see if the enhancement has already been suggested. 
If it has, add a comment to the existing issue instead of opening a new one.
- Find out whether your idea fits with the scope and aims of the project. It's up to you to make a strong case to convince the 
project's developers of the merits of this feature. Keep in mind that we want features that will be useful to the majority of our users and not just a small subset. If you're just targeting a minority of users, consider writing an add-on/plugin library.
Make sure to follow the templates given as it makes the process more smooth :D

<!-- omit in toc -->
#### How Do I Submit a Good Enhancement Suggestion?

Enhancement suggestions are tracked as [GitHub issues](https://github.com/PhilipFlyvholm/CustomCobbleGenissues).

- Use a **clear and descriptive title** for the issue to identify the suggestion.
- Provide a **step-by-step description of the suggested enhancement** in as many details as possible.
- **Describe the current behavior** and **explain which behavior you expected to see instead** and why. At this point you can also 
tell which alternatives do not work for you.
- You may want to **include screenshots and animated GIFs** which help you demonstrate the steps or point out the part which the 
suggestion is related to. You can use [this tool](https://www.cockos.com/licecap/) to record GIFs on macOS and Windows, and [this 
tool](https://github.com/colinkeenan/silentcast) or [this tool](https://github.com/GNOME/byzanz) on Linux. <!-- this should only be 
included if the project has a GUI -->
- **Explain why this enhancement would be useful** to most CustomCobbleGen users. You may also want to point out the other projects 
that solved it better and which could serve as inspiration.

### Your First Code Contribution
The project is split up into subplugins such that, for example, it is not needed to have JetsMinions purshased to help develop on the main plugin. So if you want to develop on the JetsMinions integration then you only need to modify the CustomCobbleGen-JetsMinions project while everything else is in the CustomCobbleGen-base project. 

To make your first code contribution then create a fork of the project.
The projects use gradle to build. The build process is a little different depending on the project.

**CustomCobbleGen-base:**

You can simply run `gradle build` to create a jar located in `build/libs/`. In this folder will there be located two jars. One with the dependicies build in and the other without. The one needed here is the one with the dependicies build in which is called something like `CustomCobbleGen-Base-X.X.X-all.jar`.
If you want the command `/ccg admin pastebin` to work then you need create a .env file and place it in `resources` folder with a `PASTEBIN_API` variable set to your API key.

**CustomCobbleGen-JetsMinions:**

Since this plugin requires both the CustomCobbleGen-Base and the JetsMinions plugin then these two plugins need to be added into a `libs` folder in the project root.
When you want to compile the plugin then run `gradle jar`


That's all. Happy coding!