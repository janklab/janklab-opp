# Janklab-OPP

Janklab-OPP is a redistribution of other open source libraries that are used in Matlab but not available on Maven Central from their original authors. The purpose of this project is to make these projects, in the particular versions used in Matlab, available on Maven Central so that they can be used in Maven projects, and specifically in [Matlab-JUMP](https://github.com/apjanke/matlab-jump).

This repo contains modifications of the original libraries so that they build with Maven and can be uploaded to Maven Central. No other modification is done: no bugfixes are made, and no development is done on the library source code itself.

## License

Janklab-OPP itself, to the extent that it has any code, is licensed under the BSD 3-Clause License.

The redistributed libraries are licensed under various open source licenses. See the license info in each project subdirectory for details.

| Library | License | Home Page | Comments |
| ------- | ------- | --------- | -------- |
| BlueCove | GPL | <http://www.bluecove.org> | |
| Glazed Lists | LGPL/MPL dual license | <http://www.glazedlists.com> | |
| JIDE OSS | GPL w/ Classpath Exception / free commercial dual license | <http://www.jidesoft.com/products/oss.htm> | |
| sqlite4java | Apache 2.0 | [sqlite4java on BitBucket](https://bitbucket.org/almworks/sqlite4java) | |
| TableLayout | Clearthought Software License (???) | <http://clearthought.info> | See notes in its dir for details |

## Maintainer

Janklab-OPP is maintained by [Andrew Janke](https://apjanke.net). The project home page is <https://github.com/apjanke/janklab-opp>.

See the individual project subdirectories for maintainer info on the redistributed projects.
