<!---
Unpublished copyright (c) 2015 National ICT Australia (NICTA),
ABN 62 102 206 173.  All rights reserved.

The contents of this document are proprietary to NICTA and you may not
use, copy, modify, sublicense or distribute the contents in any form
except as permitted under the terms of a separately executed licence
agreement with NICTA.

COMMERCIAL LICENSE RIGHTS
Agreement No.: FA8750-12-9-0179
Contractor's Name; Rockwell Collins, Inc.
Contractor's Address: 400 Collins Road N.E., Cedar Rapids, IA 52498

By accepting delivery of the RTOS Code and Documentation, the Licensee
agrees that the software is "commercial" computer software within the
meaning of the applicable acquisition regulations (e.g., FAR 2.101 or
DFARS 227.7202-3).  The terms and conditions of this License shall pertain
to the Licensee's use and disclosure of the software, and shall supersede
any conflicting contractual terms or conditions.

  -->

eChronos README
===============

Overview
--------

The eChronos release contains a tool `prj` which is used for configuring the RTOS and (optionally) building systems based on the RTOS.
The `prj` tool can be found in the `x86_64-unknown-linux-gnu/bin` directory.

eChronos itself is organised as a series of packages stored in the `share/packages` directory.
These are described in the following sections, and in further detail in their respective manual files.

For completeness, `LICENSE` contains the license under which this release is made available and `build_info` contains the specific build information used to uniquely identify the release.

Installation
-------------

To install eChronos simply extract the release archive in to a suitable location.
This can either be within a specific project directory, (e.g: `project/echronos`) or in a more general location (e.g: ~/local/echronos).

The `prj` binary is used to configure the RTOS, so should be installed somewhere such that it is convenient to use.
This can be done by adding `x86_64-unknown-linux-gnu/bin` to the PATH environment variable, creating a symbolic link to the `prj` binary, or simply using the full path within a build system.
E.g.: In `make`:

    PRJ := /path/to/echronos/x86_64-unknown-linux-gnu/bin/prj

Although the `prj` binary can be used anywhere on the file-system, the overall structure of the release should be maintained, as this is used to determine the location of `packages` directory.

`prj` should work on any modern Linux distribution.
It depends on zlib (version 1.2.0) and glibc (version 2.14).

Note: Mac OS X support is available, but not provided in this release.
Please advise if OS X support is desirable.

Usage
------

The RTOS can be used in two main ways.
In the full system mode the `prj` tool has full visibility to the source code base, and is used to build the full image.
In the configuration mode the `prj` tool is only used to configure the RTOS (and associated modules) rather than building the entire system firmware.

Future versions of the `prj` tools will take advantage of the full system mode to provide full system optimisations and checks, such as automatically sizing stacks.
Ideally projects that just target the RTOS will use the full system mode of `prj`.

Sub-commands
-------------

The `prj` tool is designed as a set of independent sub-commands.
The `gen` sub-command is used to generate source code for a system.
The `build` sub-command is used to build a system image, and the `load` sub-command is used to load a system image on to a target device (or simulator).

When using the `prj` tool for configuration mode only the `gen` sub-command is relevant.

Build guide
------------

This section describes the use of the `prj` tool's `build` sub-command.

Instructions below assume that the directory of `prj`, and also that of the required toolchain, are on the PATH.

The command to build a system takes the form:

    prj -o <out_dir> --search <packages_dir> --no-project build <package_name>

The package name takes the form of the dot-separated relative path of the system's .prx file from the packages directory.

For example, to build the Kochab mutex demo system for the ARMv7-M STM32F4-Discovery board, run:

    prj -o kochab-mutex-out --search share/packages --no-project build machine-stm32f4-discovery.example.kochab-mutex-demo

Global Options
---------------

The rest of this document assumes that `prj` will be used in configuration mode.

There are a number of global command line options available.
These options should be passed on the command line before the sub-command name.
E.g:

    prj --global-option sub-command

Not:

    prj sub-command --global-option

The `--project` option specifies a project description file.
The project description file is used to set project wide configuration options such as the output directory and search paths.
This option is primarily for use in full system mode, and is generally not required in configuration mode.
In full system mode the project description file defaults to `project.prj` (in the current working directory).
In configuration mode no project description file is used.

The `--no-project` option explicitly disables the use of a project file.
This is generally only useful in full system mode where a default project file is used.

The `--search-path` option allows additional package search paths to be specified on the command line.
This is generally not needed when using configuration mode.

The `--verbose` option enables additional output which may be useful when debugging.

The `--output` specifies the output directory.
This option is generally desirable when using `prj` from within another build system.

The default is `$project_output_dir/$system_name`, where `project_output_dir` is the project's output directory and `system_name` is the name of the system being built (or generated).
When no `project` is specified the `project_output_dir` will be `$pwd/out` (where `pwd` is the current working directory).

`gen` sub-command options
--------------------------

The `gen` sub-command takes a single mandatory parameter, which is the name of the system to build.
The system be either be a fully-qualified module name, or the direct path to a system description file (`.prx` file).

When using the tool in configuration mode the path to a system description file is generally used.

The system description file specifies the system in a declaration manner.
In configuration mode, the description file is only describing the configuration of the RTOS (and associated modules), rather than the full-system.

The format of the system description file is described in following sections.

An example usage of the `gen` sub-command is:

    $ prj --search share/packages --output rtos gen share/packages/machine-stm32f4-discovery/example/kochab-system.prx

This command will configure the RTOS based on the `kochab-system.prx` system description file, and generate the output files (including C and assembler source files, header files and linker scripts) into the `rtos` directory.

System Description File
------------------------

The system description file (or simply PRX file) is used to specify the system or in the case of the configuration only mode configure the RTOS related modules of the system.

Currently the system description is specified in XML, however additional formats are being considered and feedback is welcomed.

The document (root) element of the PRX file shall be `system`.
Currently only a single child element `modules` is specified.
Additional child elements may be available in the future.

The `modules` element may contain zero or more `module` child elements.
A `module` element represents the instantiation of a named module in the system.
Each `module` element may contain module specific sub-elements which configure that particular module for use within the system.
The `prj` considers the *system* to be composed of a set of configured modules.

`module` elements are processed by the `prj` tool in the order in which they appear in the PRX file.
When modules have configuration dependencies the dependent should be listed after the module on which it depends.

Each `module` element must have a `name` attribute, which names the module.
The RTOS comes with a number of built-in modules.
Custom modules may be created, however this is generally only needed when using `prj` in full system mode.

Modules may be of varying complexity.
At the simplest a module is simply a `.c` (or `.s`) file.
At the most complex the module can be a set of multiple implementation files with a customised, scripted-based generation strategy.

When using the RTOS in a configuration only mode there are four modules that will be needed: `armv7m.ctxt-switch-preempt`, `armv7m.exception-preempt`, `armv7m.vectable`, and `armv7m.rtos-kochab`.
These are described in the following sections.

`armv7m.ctxt-switch-preempt`
-----------------------------

The `armv7m.ctxt-switch-preempt` module implements the low-level context switching operation for eChronos.
This module does not have any configuration options, however as the RTOS depends on this module it must always be included in any system description.

`armv7m.exception-preempt`
---------------------------

The `armv7m.exception-preempt` module implements the low-level preemption mechanism for eChronos.
It also provides a means of generating trampoline code for exception handlers that the system designer wishes to be able to cause preemption.
These can then be installed as an exception vectors (using the `armv7m.vectable` module).

Since the RTOS depends on this module, it must always be included in any system description.

### `trampolines`

The `trampolines` element should contain `trampoline` child elements, which define each of the exception trampolines to be generated by the module.

#### `name`

Each `trampoline` element must be given a `name` child element.
This should be a unique, valid C identifier.
The name of the generated trampoline function will be `rtos_internal_exception_preempt_<name>`.

#### `handler`

Each `trampoline` element must be given a `handler` child element.
This must be the C identifier for a handler function, which must have a boolean return type.
The handler function MUST return a boolean value that is true if it has just made an action with the potential to cause a preemption, such as raising an interrupt event.
This requirement must be adhered to for the RTOS to be able to enforce its scheduling policies.

`armv7m.vectable`
------------------

Please see `share/packages/armv7m/armv7m-manual.md` for more information on the `armv7m.vectable` module.

`armv7m.rtos-kochab`
----------------------

eChronos comes in a number of different *flavors*, each of varying complexity, code size and feature set.

The RTOS flavor *kochab* supports tasks, priority scheduling, mutexes with priority inheritance, semaphores, signals, and interrupt events which can cause task preemption and trigger the sending of signals.

There are a number of configuration options that should be set:

### `prefix`

The RTOS has an API that exports a number of functions (such as `signal_send_set`).
These APIs can be prefixed to provide a name-spacing.
The suggested prefix is `rtos`, however anything can be chosen, which can help avoid namespace classes.

### `taskid_size`

The RTOS supports variables sized task identifiers.
This option sets the size of the identifier (in number of bits).
Only values of 8, 16 and 32 are supported.
Generally 8-bits (supporting up to 255 tasks) is sufficient, however there may be cases where using a larger size maps better with existing code or provides some performance benefits.

### `signalset_size`

The RTOS supports signal sets of varying sizes.
Possible values are 8, 16 and 32.
The signal set size places a limit on the number of individual signals that are available.

### `tasks`

The configuration should also include a `tasks` element, with a number of `task` child elements.
The `task` elements define each of the tasks in the system.
Each task has the following configuration elements:

#### `name`

The name of the task.
This should be a valid C identifier.
Task names must be unique within the system.

#### `function`

The entry point for the task.
The entry point should be a C function that takes zero arguments and never returns.

#### `priority`

The priority for the task.
A higher number denotes a higher priority.

#### `stack_size`

Size of the stack allocated to this task.

### `mutexes`

The `mutexes` element should contain `mutex` child elements, which define each of the mutexes in the system.

#### `name`

Each `mutex` element contains just a single child element, `name`, which should be a valid C identifier specifying the name of the mutex.

### `semaphores`

The `semaphores` element should contain `semaphore` child elements, which define each of the semaphores in the system.

#### `name`

Each `semaphore` element contains just a single child element, `name`, which should be a valid C identifier specifying the name of the semaphore.

### `signal_labels`

The `signal_labels` element should contain `signal_label` child elements, which define each of the signals in the system.

#### `name`

Each `signal` element contains just a single child element, `name`, which should be a valid C identifier specifying the name of the signal.
