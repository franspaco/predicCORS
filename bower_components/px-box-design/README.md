# Box

The Box module simply boxes off content. This module is a fork of the [inuitcss Box module](https://github.com/inuitcss/objects.box).

## Demo

You can review box styles and recommended markup here: https://github.build.ge.com/pages/PXd/px-box-design

## Sass Docs

You can review Sass Documentation here: https://github.build.ge.com/pages/PXd/px-box-design/sassdoc

## Dependencies

Px's Box module depends on one other Px and one other inuitcss module:

* [trumps.clearfix](https://github.com/inuitcss/trumps.clearfix)
* [px-defaults-design](https://github.build.ge.com/PXd/px-defaults-design)

## Installation

Install this module and its dependencies using bower:

    bower install --save https://github.build.ge.com/PXd/px-box-design.git

Once installed, `@import` into your project's Sass file in its Objects layer:

    @import "px-box-design/_objects.box.scss";

## Usage

These flags are available and, if needed, should be set to `true` prior to importing the module:

    $inuit-enable-box--flush
    $inuit-enable-box--tiny
    $inuit-enable-box--small
    $inuit-enable-box--large
    $inuit-enable-box--huge

## Options

Basic usage of the Box object uses the required class:

    <div class="box">
        Foo Bar Baz
    </div>

Other, optional classes can supplement the required base classes. These classes are available if the variable flags listed above are set to `true`:

* `box--flush`: remove all padding from boxes.
* `box--[tiny|small|large|huge]`: alter the padding size on boxes.

For example:

    <div class="box box--large">
        Foo Bar Baz
    </div>
