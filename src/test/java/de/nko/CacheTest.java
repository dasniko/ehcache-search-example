package de.nko;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Results;

import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class CacheTest {

    private static Cache cache = CacheManager.getInstance().getCache("persons");
    private static Attribute<String> lastname = cache.getSearchAttribute("lastname");
    private static Attribute<String> gender = cache.getSearchAttribute("gender");
    private static Attribute<String> country = cache.getSearchAttribute("country");

    @BeforeClass
    public static void init() throws Exception {
        List<Person> persons = createPersons();
        populateCache(persons);
    }

    @Test
    public void searchLastnameSmith() throws Exception {
        log.info("Preparing search for lastname='Smith'...");
        long start = System.currentTimeMillis();
        Query query = cache.createQuery().includeValues().addCriteria(lastname.eq("Smith")).end();
        performSearch(start, query);
    }

    @Test
    public void searchGenderM() throws Exception {
        log.info("Preparing search for gender='M'...");
        long start = System.currentTimeMillis();
        Query query = cache.createQuery().includeValues().addCriteria(gender.eq("M")).end();
        performSearch(start, query);
    }

    @Test
    public void searchCountryGermany() throws Exception {
        log.info("Preparing search for country='Germany'...");
        long start = System.currentTimeMillis();
        Query query = cache.createQuery().includeValues().addCriteria(country.eq("Germany")).end();
        performSearch(start, query);
    }

    @Test
    public void searchGenderFCountryFrance() throws Exception {
        log.info("Preparing search for gender='F' and country='France'...");
        long start = System.currentTimeMillis();
        Query query = cache.createQuery().includeValues().addCriteria(gender.eq("F")).addCriteria(country.eq("France"))
                .end();
        performSearch(start, query);
    }

    @Test
    public void searchGenderMLastnameSchmidt() throws Exception {
        log.info("Preparing search for gender='M' and lastname='Schmidt'...");
        long start = System.currentTimeMillis();
        Query query = cache.createQuery().includeValues().addCriteria(gender.eq("M"))
                .addCriteria(lastname.eq("Schmidt")).end();
        performSearch(start, query);
    }

    private void performSearch(long start, Query query) {
        Results results = query.execute();
        int count = results.size();
        long duration = System.currentTimeMillis() - start;
        log.info("Searchresult: found {} persons in {} ms.", count, duration);
        results.discard();
    }

    private static void populateCache(List<Person> persons) {
        log.info("Put persons into cache...");
        int n = persons.size();
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            for (Person p : persons) {
                int k = p.getId() + n * i;
                Element e = new Element(k, p);
                cache.put(e);
            }
        }
        long duration = System.currentTimeMillis() - start;
        log.info("All persons cached in {}ms.", duration);
        log.info("Cache size is {} elements", cache.getStatistics().getLocalHeapSize());
        // log.info("Cache size is {}MB", cache.getStatistics().getLocalHeapSizeInBytes() / 1024 / 1024);
    }

    private static List<Person> createPersons() throws Exception {
        log.info("Creating persons from repository...");
        List<Person> persons = new ArrayList<Person>();

        InputStream is = CacheTest.class.getResourceAsStream("/mock_persons.csv");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = br.readLine()) != null) {
            String[] strings = line.split(",");
            Person p = new Person();
            p.setId(Integer.parseInt(strings[0]));
            p.setFirstname(strings[1]);
            p.setLastname(strings[2]);
            p.setGender(strings[3]);
            p.setCountry(strings[4]);
            persons.add(p);
        }

        br.close();
        is.close();

        log.info("Created {} persons from repository.", persons.size());
        return persons;
    }

}
