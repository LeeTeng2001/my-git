<div id="top"></div>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="doc/img/logo.png" alt="Logo" width="160" height="80">
  </a>

<h3 align="center">An Educational Version Control System</h3>

<p align="center">
    A version control system like Git!
    <br />
    <a href="https://github.com/othneildrew/Best-README-Template"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/othneildrew/Best-README-Template">View Demo</a>
    ·
    <a href="https://github.com/othneildrew/Best-README-Template/issues">Report Bug</a>
    ·
    <a href="https://github.com/othneildrew/Best-README-Template/issues">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

![demo](doc/demo%201.gif)

Git is a useful version control tool, have you ever wonder about the algorithm behind it? This is my attempt at creating a small & functional version of a version control tool like Git.

Main features:

* It's small, the whole codebase is around 2k lines
* You have all the important command like `init`, `commit`, `status`, `restore` and so on.
* It's written in Java with some modern syntax

I'm planning on writing an article about it, so stay tuned!

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- GETTING STARTED -->

## Getting Started

### Prerequisites

You should have a working jdk distribution in your machine, I'm using OpenJDK17

* mac brew package manager
  
  ```sh
  brew install java
  ```

### Installation

You can either build the project yourself or use the existing build

* Clone this repository and open the project in intellij, go to `build > build artifact > my-git`
* Or build using `maven build`
* Or run existing build 
  ```sh 
  cd <project-directory>
  java -jar out/artifacts/my_git_jar/my-git.jar
  
  # For convenience, you could also setup an alias to the jar path like this
  alias git-run=java\ -jar\ <abs_path>/out/artifacts/my_git_jar/my-git.jar
  # and run the version control command like this
  git-run <some_command>
  ```

### Developing

You can directly run the maven project in Intellij or run the shell script `git-run.sh` and pass in corresponding arguments

  ```sh 
  cd <project-directory>
  chmod +x git-run
  ./git-run <some_command>
  ```

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->

## Usage

For every command you could specify `-h` or `--help` flag to show helps, etc:

```sh
➜ git-run -h
Usage: git [-hV] [COMMAND]
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.
Commands:
  init         Initialise a git repository with author name and email
  cat-file     Print content of git object
  hash-object  Compute object ID and optionally create blob file
  log          Display history from a given commit.
  ls-tree      Pretty-print a tree object.
  restore      Restore a commit inside of a directory. Caveat, doesn't restore
                 executable permission bit
  show-ref     List all references
  tag          Show tags or create a new tag for an object, usually pointing to
                 commit
  status       Show changes starting from directory, ONLY show changed files,
                 does not detect changes if content is the same
  commit       Save current state
```

_For more examples, please refer to the [Documentation](https://example.com)_

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- ROADMAP -->

## Roadmap

- [x] Write unit tests
- [ ] Write article
- [ ] Add branching support
- [ ] Multi-language Support
  - [ ] Chinese


<p align="right">(<a href="#top">back to top</a>)</p>

<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- CONTACT -->

## Contact

Bryan Lee - leeteng2001@sjtu.edu.cn

<!-- Project Link: [https://github.com/your_username/repo_name](https://github.com/your_username/repo_name) -->

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- ACKNOWLEDGMENTS -->

## Acknowledgments

Special thanks to the following resources

* [Git format documentation](https://github.com/git/git/tree/master/Documentation/technical)
* [MD Book](https://github.com/rust-lang/mdBook)
* [PicoCLI](https://picocli.info/)

<p align="right">(<a href="#top">back to top</a>)</p>

