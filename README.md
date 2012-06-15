# facepalm

Database migration and management tool.

## Usage

```
# Reinitialize the database using the database tarball from the latest QA drop.
facepalm -h hostname -U username -d database -q latest

# Explicitly fetch the database tarball from the QA drop on 6/15/2012.
facepalm -h hostname -U username -d database -q 2012-06-15

# Reinitiaize the database using a local database tarball.
facepalm -h hostname -U username -d database -f /path/to/database.tar.gz

# Obtain the database tarball from the latest build of a Jenkins job.
facepalm -h hostname -U username -d database -j jobname

# Upgrade the database from the existing version.
facepalm -m update -h hostname -U username -d database

# Obtaining help.
facepalm -?
```

## Required Arguments

All of the arguments that are required have default values, so these arguments
may not have to be explicitly defined on the command line unless the default
settings won't work in your case.

### -m --mode

Default value: `init`

This argument indicates whether facepalm should update or completely
initialize the database.  If the mode is set to `update` then the database
schema will be updated without clobbering any data in the database (unless
some database rows are _supposed_ to be clobbered as part of a database
conversion).  If this mode is set to `init` then the database will be
completely reinitialized.

### -h --host

Default Value: `localhost`

This argument indicates the name of host where the database resides.  If this
argument is not specified, facepalm will attempt to connect to the database
server on the local host.

### -p --port

Default Value: `5432`

This argument indicates which port number to use when attempting to connect to
the datase.  If this argument is not specified, facepalm will attempt to
connect to port `5432` (the default listen port for PostgreSQL).

### -d --database

Default Value: `de`

This argument indicates the name of the database to update.  If this argument
is not specified, faceplam will update the `de` database.

### -U --user

Default Value: `de`

This argument indicates the username to use when authenticating to the
database.  If this argument is not specified, facepalm will use `de` for the
username.  Note that the database user must own both the database and the
public schema in the database.

There is no command-line option to specify the password.  Instead, facepalm
will attempt to look up the password in the `.pgpass` file.  If the password
can't be obtained from `.pgpass`, facepalm will prompt the user for the
password.  Because facepalm uses JDBC to connect to the database, only one
connection to the database is established per facepalm invocation.  Because of
this, there will be at most one password prompt per facepalm invocation.

## Options

### -j --job

Default Value: `database`

This argument indicates the name of a Jenkins job from which the database
initialization scripts can be obtained.  At the time of this writing, valid
job names are `database` and `database-dev`.

This argument is actually one of three mutually exclusive command-line
arguments that can be used to indicate where facepalm should obtain its
database initialization files.  The other two options are `-q`, which is
equivalent to `--qa-drop`, and `-f`, which is equivalent to `--filename`.
Note that at least one of these three command-line options must have a value
if facepalm is executed in `init` mode.  If none of these three command-line
options is specified in `init` mode then facepalm will default to using the
`-j` option with the default value of `database`, meaning that facepalm will
obtain the database initialization files from the build artifact for the
latest `database` project build in iPlant's Jenkins deployment.

### -q --qa-drop

This argument indicates the QA drop from which the database initialization
scripts should be obtained.  This can either be the date of the QA drop in
`YYYY-MM-DD` format (for example, `2012-06-12`) or the literal string `latest`
to indicate that facepalm should obtain the database initialization scripts
from the most recent QA drop.

This argument is actually one of three mutually exclusive command-line
arguments that can be used to indicate where facepalm should obtain its
database initialization files.  The other two options are `-J`, which is
equivalent to `--job`, and `-f`, which is equivalent to `--filename`.  Note
that at least one of these three command-line options must have a value if
facepalm is executed in `init` mode.  If none of these three command-line
options is specified in `init` mode then facepalm will default to using the
`-j` option with the default value of `database`, meaning that facepalm will
obtain the database initialization files from the build artifact for the
latest `database` project build in iPlant's Jenkins deployment.

### -f --filename

This argument indicates that facepalm should obtain the database
initialization scripts from a tarball on the local file system.  This option
is most commonly used during development testing so that updates to the
database initialization scripts may be tested without having to push the
script changes into the git repository first.

This argument is actually one of three mutually exclusive command-line
arguments that can be used to indicate where facepalm should obtain its
database initialization files.  The other two options are `-j`, which is
equivalent to `--job`, and `-q`, which is equivalent to `--qa-drop`.  Note
that at least one of these three command-line options must have a value if
facepalm is executed in `init` mode.  If none of these three command-line
options is specified in `init` mode then facepalm will default to using the
`-j` option with the default value of `database`, meaning that facepalm will
obtain the database initialization files from the build artifact for the
latest `database` project build in iPlant's Jenkins deployment.

### -? --help --no-help

The `-?` and `--help` options can be used to tell facepalm to display a help
message.  The `--no-help` message can be used to indicate that help should not
be displayed, but this is the default behavior anyway.

### --debug --no-debug

The `--debug` option can be used to tell facepalm to display additional
debugging information, which can be helpful in troubleshooting database
initialization problems.  The `--no-debug` option can be used to disable
debugging, but this is the default behavior anyway.

## Diagnostics

### ::required-options-missing

No value was provided for a required option.  This error is unlikely to occur
in practice because all of the required arguments have default values.  If
this does occur, verify that the command line is correct.

### ::build-artifact-retrieval-failed

The utility attempted to get the database build artifact from a remote URL and
a status that was less than 200 or greater than 299 was returned.  Verify that
the specified QA drop or Jenkins job name is correct.

### ::build-artifact-expansion-failed

The utility attempted to extract the database initialization files from the
database build artifact and `tar` returned a non-zero exit status.  Verify
that the build artifact is a valid tar file.

### ::temp-directory-creation-failure

The utility attempted and was unable to create a temporary directory within
the current working directory.  Verify that you have write permission to the
current working directory.

### ::unknown-mode

The specified mode is not recognized by the utility.  Verify that the command
line is correct.

### Other Errors

Other errors represent uncaught exceptions.  The most likely cause of these
errors is a failure to connect to or update the database.  Verify that the
command line is correct, paying close attention to the database connection
settings.  It might also be helpful to review the exception itself to find out
if it contains any useful information.  If you're unable to find the cause of
the problem, please send a copy of the command line, the exception and the
full stack trace, if there is one, to the Core Software group.

## Bugs and Limitations

There are no known bugs in facepalm at this time.  Please report problems to
the Core Software group.  Patches are welcome.

## License and Copyright

Copyright (c) 2012, The Arizona Board of Regents on behalf of The University
of Arizona

All rights reserved.

Developed by: iPlant Collaborative at BIO5 at The University of Arizona
http://www.iplantcollaborative.org

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 * Neither the name of the iPlant Collaborative, BIO5, The University of
   Arizona nor the names of its contributors may be used to endorse or promote
   products derived from this software without specific prior written
   permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
