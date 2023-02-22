# Release

Release to the maven center, you should follow these steps.



## 1. GPG Key

```
brew install gpg

gpg --generate-key
```



To see generated gpg key

```
gpg -k
```

Last 8 characters of pub key is your `Key ID`,  like `8D65454A`

```
pub   ed25519 2023-02-22 [SC] [有效至：2025-02-21]
      811E7040F84D1BD44BEC15EE6DE91A6E8D65454A
uid   ... ...
sub   ... ...
```

Export gpg file,  replace your own Key ID: 

```
gpg --export-secret-keys 8D65454A > secret.gpg
```

Send to public key server:

```
gpg --keyserver keyserver.ubuntu.com --send-keys 8D65454A
```



Add gpg secret file into treetops core module.



## 2. Modify Properties



```
group=io.github.horoc
version=1.0-SNAPSHOT  // version your want to release

ossrhUsername=UserName
ossrhPassword=Password

signing.keyId=KeyId // key id, last 8 characters of your public key
signing.password=PublicKeyPassword // password of your gpg key
signing.secretKeyRingFile=PathToYourKeyRingFile // gpg file path, for treetops-core, it should be `secret.gpg`
```



## 3. Run Publish

```
./gradlw publish
```



## 4. Check Publish Status

for SNAPSHOT:

```
https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/horoc
```

for Release:

```
https://s01.oss.sonatype.org/
```



## 5. Clean Up Your Modification

1. **You should clean the content of the gradle.properties to avoid publishing your password to the GitHub repository.**
2. **You should delete the gpg file which you add to the project**

