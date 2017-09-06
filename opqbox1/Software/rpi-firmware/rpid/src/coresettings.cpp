#include "coresettings.hpp"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>
#include <fstream>
#include <boost/algorithm/string/trim.hpp>
#include <boost/algorithm/string/split.hpp>
#include <boost/foreach.hpp>
#include <boost/lexical_cast.hpp>

OpqSettings* OpqSettings::instance_ = NULL;

OpqSettings::OpqSettings()
{
}

OpqSettings* OpqSettings::Instance()
{
    if(instance_ == NULL)
        instance_ = new OpqSettings();
    return instance_;
}

bool OpqSettings::setSetting(std::string key, OpqSetting value)
{
    boost::algorithm::trim(key);
    boost::unique_lock< boost::shared_mutex > lock(mutex_);
    settings_[key]= value;
    return true;
}

OpqSetting OpqSettings::getSetting(std::string key)
{
    boost::algorithm::trim(key);
    boost::shared_lock< boost::shared_mutex > lock(mutex_);
    std::map<std::string, OpqSetting>::iterator pos = settings_.lower_bound(key);
    if(pos != settings_.end() && !(settings_.key_comp()(key, pos->first)))
    {
        return pos->second;
    }
    return "";
}

bool OpqSettings::loadFromFile(std::string filename)
{
    std::ifstream infile(filename.c_str());
    std::string line;
    if(!infile.is_open())
        return false;
    boost::shared_lock< boost::shared_mutex > lock(mutex_);
    while (std::getline(infile, line))
    {
        vector<string> strs;
        if(line[0] == '#')
            continue;
        boost::split(strs,line,boost::is_any_of(":"));
        if(strs.size() < 3)
            continue;
        std::string key = strs[0];
        std::string type = strs[1];
        std::string value = strs[2];
        for(int i = 3; i< strs.size(); i++)
        {
            value +=":" +  strs[i];
        }
        boost::algorithm::trim(key);
        boost::algorithm::trim(type);
        boost::algorithm::trim(value);
        if(key.length() == 0 || type.length() == 0 || value.length() == 0)
            continue;
        OpqSetting setValue;
        try
        {
            switch (type.at(0))
            {
            case 'U':
                setValue = boost::lexical_cast<uint64_t>(value);
                break;
            case 'F':
                setValue = boost::lexical_cast<double>(value);
                break;
            case 'I':
                setValue = boost::lexical_cast<int>(value);
                break;
            case 'S':
                setValue = value;
                break;
            case 'B':
                if(value == "TRUE")
                    setValue = true;
                else
                    setValue = false;
                break;
            default:
                throw boost::bad_lexical_cast();
            }
        }
        catch(boost::bad_lexical_cast &e)
        {
            continue;
        }
        settings_[key] = setValue;
    }
    return true;
}

bool OpqSettings::saveToFile(std::string filename)
{
    typedef std::map<string, OpqSetting> map_type;
    std::ofstream ofile(filename.c_str());
    std::string line;
    if(!ofile.is_open())
        return false;

    BOOST_FOREACH(const map_type::value_type& myPair, settings_)
    {
        OpqSetting val = myPair.second;
        std::string key = myPair.first;
        line = key + ":";
        switch (val.which())
        {
        case 0: //uint64_t
            line +="U:" + boost::lexical_cast<std::string>(boost::get<uint64_t>(val));
            break;
        case 1: //float
            line +="F:" + boost::lexical_cast<std::string>(boost::get<double>(val));
            break;
        case 2: //int
            line +="I:" + boost::lexical_cast<std::string>(boost::get<int>(val));
            break;
        case 3: //string
            line +="S:" + boost::get<std::string>(val);
            break;
        case 4: //bool
            line +="B:";
            if(boost::get<bool>(val))
                line+="TRUE";
            else
                line+="FALSE";
            break;
        }
        ofile << line << std::endl;
    }
    return true;
}

bool OpqSettings::isSet(std::string key)
{
    boost::algorithm::trim(key);
    boost::shared_lock< boost::shared_mutex > lock(mutex_);
    std::map<std::string, OpqSetting>::iterator pos = settings_.lower_bound(key);
    if(pos != settings_.end() && !(settings_.key_comp()(key, pos->first)))
    {
        return true;
    }
    return false;
}

OpqSetting OpqSettings::erase(std::string key)
{
    boost::algorithm::trim(key);
    boost::unique_lock< boost::shared_mutex > lock(mutex_);
    OpqSetting out;
    std::map<std::string, OpqSetting>::iterator pos = settings_.lower_bound(key);
    if(pos != settings_.end() && !(settings_.key_comp()(key, pos->first)))
    {
        out = pos->second;
        settings_.erase(pos);
    }
    return out;
}

void OpqSettings::clear()
{
    boost::unique_lock< boost::shared_mutex > lock(mutex_);
    settings_.clear();
}
