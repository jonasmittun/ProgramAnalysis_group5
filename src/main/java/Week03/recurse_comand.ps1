$mypath1 = Split-Path -Parent $MyInvocation.MyCommand.Path
$mypath = $mypath1 + "\com\mojang\ld22"
Get-ChildItem –Path $mypath –Recurse |

Foreach-Object {
    $extn = [IO.Path]::GetExtension($_)
    if ($extn -eq ".class" ) {
        $oldname = $_.FullName
        $newname = $oldname.replace($extn, ".json")
        jvm2json -s $oldname -t $newname
    }
}